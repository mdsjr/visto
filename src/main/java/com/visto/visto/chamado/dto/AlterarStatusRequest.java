package com.visto.visto.chamado.dto;

import com.visto.visto.domain.chamado.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @NotNull(message = "Status é obrigatório")
        StatusChamado status
) {
}
