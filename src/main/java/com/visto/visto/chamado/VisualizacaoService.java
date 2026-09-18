package com.visto.visto.chamado;

import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.chamado.VisualizacaoChamado;
import com.visto.visto.domain.usuario.Usuario;
import com.visto.visto.repository.VisualizacaoChamadoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controla quem (técnicos) está com cada chamado aberto no momento, para alertar
 * quando dois técnicos acessam o mesmo chamado simultaneamente.
 */
@Service
public class VisualizacaoService {

    private final VisualizacaoChamadoRepository repository;
    private final int janelaMinutos;

    public VisualizacaoService(VisualizacaoChamadoRepository repository,
                               @Value("${visto.chamado.janela-visualizacao-min:5}") int janelaMinutos) {
        this.repository = repository;
        this.janelaMinutos = janelaMinutos;
    }

    /**
     * Registra/renova a visualização do técnico e devolve os nomes dos OUTROS técnicos
     * ativos no mesmo chamado dentro da janela.
     */
    @Transactional
    public List<String> registrarERetornarOutros(Chamado chamado, Usuario tecnico) {
        LocalDateTime agora = LocalDateTime.now();

        VisualizacaoChamado visualizacao = repository
                .findByChamadoIdAndTecnicoId(chamado.getId(), tecnico.getId())
                .orElseGet(() -> VisualizacaoChamado.builder().chamado(chamado).tecnico(tecnico).build());
        visualizacao.setUltimaAtividade(agora);
        repository.save(visualizacao);

        return outrosAtivos(chamado.getId(), tecnico.getId(), agora);
    }

    @Transactional(readOnly = true)
    public List<String> outrosAtivos(Long chamadoId, Long tecnicoId) {
        return outrosAtivos(chamadoId, tecnicoId, LocalDateTime.now());
    }

    /** Técnico saiu do chamado: remove seu registro para não gerar alerta falso. */
    @Transactional
    public void sair(Long chamadoId, Usuario tecnico) {
        repository.deleteByChamadoIdAndTecnicoId(chamadoId, tecnico.getId());
    }

    /** Limpeza oportunista de registros antigos (chamada a cada registro). */
    @Transactional
    public void limparExpirados() {
        repository.deleteByUltimaAtividadeBefore(LocalDateTime.now().minusMinutes(janelaMinutos * 12L));
    }

    private List<String> outrosAtivos(Long chamadoId, Long tecnicoId, LocalDateTime agora) {
        LocalDateTime limite = agora.minusMinutes(janelaMinutos);
        return repository.findByChamadoIdAndUltimaAtividadeAfter(chamadoId, limite).stream()
                .filter(v -> !v.getTecnico().getId().equals(tecnicoId))
                .map(v -> v.getTecnico().getNome())
                .sorted()
                .toList();
    }
}
