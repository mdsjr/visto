package com.visto.visto.repository;

import com.visto.visto.domain.chamado.VisualizacaoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisualizacaoChamadoRepository extends JpaRepository<VisualizacaoChamado, Long> {

    Optional<VisualizacaoChamado> findByChamadoIdAndTecnicoId(Long chamadoId, Long tecnicoId);

    /** Visualizações ainda dentro da janela de atividade. */
    List<VisualizacaoChamado> findByChamadoIdAndUltimaAtividadeAfter(Long chamadoId, LocalDateTime limite);

    void deleteByChamadoIdAndTecnicoId(Long chamadoId, Long tecnicoId);

    void deleteByUltimaAtividadeBefore(LocalDateTime limite);
}
