package com.visto.visto.chamado.dto;

import com.visto.visto.domain.chamado.PrioridadeChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AbrirChamadoRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        /** Opcional. Padrão: MEDIA. */
        PrioridadeChamado prioridade
) {
}
