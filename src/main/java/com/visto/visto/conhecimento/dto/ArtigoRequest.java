package com.visto.visto.conhecimento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtigoRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "Problema é obrigatório")
        @Size(max = 2000, message = "Problema deve ter no máximo 2000 caracteres")
        String problema,

        @Size(max = 2000, message = "Cenário deve ter no máximo 2000 caracteres")
        String cenario,

        String itensAvaliados,

        @NotBlank(message = "Procedimento é obrigatório")
        String procedimento,

        @Size(max = 500, message = "Tags devem ter no máximo 500 caracteres")
        String tags,

        /** Opcional: id do chamado que originou o artigo. */
        Long chamadoOrigemId
) {
}
