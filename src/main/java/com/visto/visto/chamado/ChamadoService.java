package com.visto.visto.chamado;

import com.visto.visto.chamado.dto.AbrirChamadoRequest;
import com.visto.visto.chamado.dto.AcompanhamentoPublicoResponse;
import com.visto.visto.chamado.dto.ChamadoResponse;
import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.chamado.PrioridadeChamado;
import com.visto.visto.domain.chamado.StatusChamado;
import com.visto.visto.domain.usuario.Usuario;
import com.visto.visto.exception.RecursoNaoEncontradoException;
import com.visto.visto.exception.RegraNegocioException;
import com.visto.visto.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final VisualizacaoService visualizacaoService;

    @Transactional
    public ChamadoResponse abrir(AbrirChamadoRequest request, Usuario solicitante) {
        Chamado chamado = Chamado.builder()
                .titulo(request.titulo().trim())
                .descricao(request.descricao().trim())
                .prioridade(request.prioridade() != null ? request.prioridade() : PrioridadeChamado.MEDIA)
                .status(StatusChamado.ABERTO)
                .solicitante(solicitante)
                .build();
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    /**
     * Técnico/admin enxerga a fila completa; usuário comum só os próprios chamados.
     */
    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(Usuario usuario, StatusChamado status, PrioridadeChamado prioridade,
                                        Pageable pageable) {
        Page<Chamado> pagina;
        if (usuario.isTecnicoOuAdmin()) {
            // Fila do técnico: mais crítico primeiro, depois o mais antigo
            Sort ordem = Sort.by(Sort.Order.desc("nivelPrioridade"), Sort.Order.asc("dataAbertura"));
            Specification<Chamado> filtro = ChamadoRepository.comStatus(status)
                    .and(ChamadoRepository.comPrioridade(prioridade));
            pagina = chamadoRepository.findAll(filtro, comOrdem(pageable, ordem));
        } else {
            // Usuário comum: só os próprios, mais recente primeiro
            Sort ordem = Sort.by(Sort.Order.desc("dataAbertura"));
            Specification<Chamado> filtro = ChamadoRepository.doSolicitante(usuario.getId())
                    .and(ChamadoRepository.comStatus(status));
            pagina = chamadoRepository.findAll(filtro, comOrdem(pageable, ordem));
        }
        return pagina.map(ChamadoResponse::de);
    }

    /**
     * Detalhe do chamado. Para técnicos/admins, registra que o chamado está aberto por ele
     * e devolve o alerta caso outro técnico também esteja nele agora.
     */
    @Transactional
    public ChamadoResponse buscar(Long id, Usuario usuario) {
        Chamado chamado = obterComPermissao(id, usuario);
        if (!usuario.isTecnicoOuAdmin()) {
            return ChamadoResponse.de(chamado);
        }
        List<String> outros = visualizacaoService.registrarERetornarOutros(chamado, usuario);
        return ChamadoResponse.de(chamado, outros);
    }

    /** Técnico fechou a tela do chamado. */
    @Transactional
    public void sairDoChamado(Long id, Usuario tecnico) {
        obter(id);
        visualizacaoService.sair(id, tecnico);
    }

    @Transactional(readOnly = true)
    public AcompanhamentoPublicoResponse acompanhar(String codigoPublico) {
        Chamado chamado = chamadoRepository.findByCodigoPublico(codigoPublico)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Chamado", codigoPublico));
        return AcompanhamentoPublicoResponse.de(chamado);
    }

    /** Técnico assume o chamado para si. */
    @Transactional
    public ChamadoResponse assumir(Long id, Usuario tecnico) {
        Chamado chamado = obter(id);
        if (chamado.getStatus().isEncerrado()) {
            throw new RegraNegocioException("Chamado já está " + chamado.getStatus());
        }
        if (chamado.getTecnico() != null && !chamado.isTecnicoResponsavel(tecnico)) {
            throw new RegraNegocioException("Chamado já está sendo atendido por " + chamado.getTecnico().getNome());
        }
        chamado.setTecnico(tecnico);
        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        }
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, StatusChamado novoStatus, Usuario usuario) {
        Chamado chamado = obter(id);
        StatusChamado atual = chamado.getStatus();

        boolean solicitanteFechando = chamado.isSolicitante(usuario) && novoStatus == StatusChamado.FECHADO;
        if (!usuario.isTecnicoOuAdmin() && !solicitanteFechando) {
            throw new AccessDeniedException("Somente técnicos alteram o status do chamado");
        }
        if (!atual.podeIrPara(novoStatus)) {
            throw new RegraNegocioException("Não é possível mudar de " + atual + " para " + novoStatus);
        }
        if (novoStatus == StatusChamado.EM_ATENDIMENTO && chamado.getTecnico() == null && usuario.isTecnicoOuAdmin()) {
            chamado.setTecnico(usuario);
        }

        chamado.setStatus(novoStatus);
        if (novoStatus == StatusChamado.RESOLVIDO) {
            chamado.setDataResolucao(LocalDateTime.now());
        } else if (novoStatus == StatusChamado.EM_ATENDIMENTO) {
            chamado.setDataResolucao(null);
        }
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponse alterarPrioridade(Long id, PrioridadeChamado prioridade) {
        Chamado chamado = obter(id);
        if (chamado.getStatus().isEncerrado()) {
            throw new RegraNegocioException("Não é possível alterar a prioridade de um chamado encerrado");
        }
        chamado.setPrioridade(prioridade);
        chamado.setPrazoSla(chamado.getDataAbertura().plusHours(prioridade.getSlaHoras()));
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    // ---- helpers ----

    private static Pageable comOrdem(Pageable pageable, Sort ordem) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ordem);
    }

    Chamado obter(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Chamado", id));
    }

    Chamado obterComPermissao(Long id, Usuario usuario) {
        Chamado chamado = obter(id);
        if (!usuario.isTecnicoOuAdmin() && !chamado.isSolicitante(usuario)) {
            // 404 em vez de 403 para não revelar a existência de chamados de terceiros
            throw RecursoNaoEncontradoException.de("Chamado", id);
        }
        return chamado;
    }
}
