package com.visto.visto.repository;

import com.visto.visto.domain.chamado.ComentarioChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioChamadoRepository extends JpaRepository<ComentarioChamado, Long> {

    List<ComentarioChamado> findByChamadoIdOrderByDataCriacaoAscIdAsc(Long chamadoId);

    List<ComentarioChamado> findByChamadoIdAndInternoFalseOrderByDataCriacaoAscIdAsc(Long chamadoId);

    long countByChamadoId(Long chamadoId);

    void deleteByChamadoId(Long chamadoId);
}
