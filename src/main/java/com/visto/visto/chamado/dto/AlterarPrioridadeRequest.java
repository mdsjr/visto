package com.visto.visto.chamado.dto;

import com.visto.visto.domain.chamado.PrioridadeChamado;
import jakarta.validation.constraints.NotNull;

public record AlterarPrioridadeRequest(
        @NotNull(message = "Prioridade é obrigatória")
        PrioridadeChamado prioridade
) {
}
