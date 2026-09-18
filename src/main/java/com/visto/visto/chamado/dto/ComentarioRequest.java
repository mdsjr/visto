package com.visto.visto.chamado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "Mensagem é obrigatória")
        @Size(max = 5000, message = "Mensagem deve ter no máximo 5000 caracteres")
        String mensagem,

        /** Nota interna (só técnicos/admins veem). Apenas técnicos/admins podem marcar. Padrão: false. */
        boolean interno
) {
}
