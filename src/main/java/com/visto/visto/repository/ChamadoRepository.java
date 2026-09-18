package com.visto.visto.repository;

import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.chamado.PrioridadeChamado;
import com.visto.visto.domain.chamado.StatusChamado;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    Optional<Chamado> findByCodigoPublico(String codigoPublico);

    long countByStatus(StatusChamado status);

    // ---- Specifications (filtros opcionais, sem parâmetro nulo em JPQL) ----

    static Specification<Chamado> comStatus(StatusChamado status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    static Specification<Chamado> comPrioridade(PrioridadeChamado prioridade) {
        return (root, query, cb) -> prioridade == null ? cb.conjunction() : cb.equal(root.get("prioridade"), prioridade);
    }

    static Specification<Chamado> doSolicitante(Long solicitanteId) {
        return (root, query, cb) -> cb.equal(root.get("solicitante").get("id"), solicitanteId);
    }
}
