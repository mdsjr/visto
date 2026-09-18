package com.visto.visto.chamado;

import com.visto.visto.chamado.dto.ComentarioRequest;
import com.visto.visto.chamado.dto.ComentarioResponse;
import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.chamado.ComentarioChamado;
import com.visto.visto.domain.chamado.StatusChamado;
import com.visto.visto.domain.usuario.Usuario;
import com.visto.visto.exception.RegraNegocioException;
import com.visto.visto.repository.ChamadoRepository;
import com.visto.visto.repository.ComentarioChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Comunicação dentro do chamado: solicitante e técnico trocam mensagens;
 * técnicos podem deixar notas internas invisíveis ao solicitante.
 */
@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioChamadoRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final ChamadoService chamadoService;

    @Transactional
    public ComentarioResponse comentar(Long chamadoId, ComentarioRequest request, Usuario autor) {
        // Reaproveita a regra de visibilidade: usuário comum só acessa os próprios chamados (404 caso contrário)
        Chamado chamado = chamadoService.obterComPermissao(chamadoId, autor);

        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("Chamado fechado não aceita novos comentários");
        }
        if (request.interno() && !autor.isTecnicoOuAdmin()) {
            throw new AccessDeniedException("Somente técnicos podem criar notas internas");
        }

        ComentarioChamado comentario = ComentarioChamado.builder()
                .chamado(chamado)
                .autor(autor)
                .mensagem(request.mensagem().trim())
                .interno(request.interno())
                .build();
        comentario = comentarioRepository.save(comentario);

        // Solicitante respondeu ao técnico: chamado volta para a fila de atendimento
        if (chamado.isSolicitante(autor) && chamado.getStatus() == StatusChamado.AGUARDANDO_USUARIO) {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        }
        // Qualquer comentário público conta como atualização do chamado (preUpdate ajusta dataAtualizacao)
        chamadoRepository.save(chamado);

        return ComentarioResponse.de(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar(Long chamadoId, Usuario usuario) {
        chamadoService.obterComPermissao(chamadoId, usuario);

        List<ComentarioChamado> comentarios = usuario.isTecnicoOuAdmin()
                ? comentarioRepository.findByChamadoIdOrderByDataCriacaoAscIdAsc(chamadoId)
                : comentarioRepository.findByChamadoIdAndInternoFalseOrderByDataCriacaoAscIdAsc(chamadoId);

        return comentarios.stream().map(ComentarioResponse::de).toList();
    }
}
