package com.visto.visto.repository;

import com.visto.visto.domain.conhecimento.ArtigoConhecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtigoConhecimentoRepository extends JpaRepository<ArtigoConhecimento, Long> {

    /**
     * Busca textual simples (case-insensitive) em título, problema, cenário e tags.
     * O parâmetro já deve vir no formato %termo%.
     */
    @Query("""
            select a from ArtigoConhecimento a
            where lower(a.titulo) like :termo
               or lower(a.problema) like :termo
               or lower(coalesce(a.cenario, '')) like :termo
               or lower(coalesce(a.tags, '')) like :termo
            order by a.dataAtualizacao desc
            """)
    Page<ArtigoConhecimento> buscar(@Param("termo") String termo, Pageable pageable);

    Page<ArtigoConhecimento> findAllByOrderByDataAtualizacaoDesc(Pageable pageable);

    boolean existsByChamadoOrigemId(Long chamadoId);
}
