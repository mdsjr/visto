package com.visto.visto.chamado.dto;

import com.visto.visto.auth.dto.UsuarioResponse;
import com.visto.visto.domain.chamado.ComentarioChamado;

import java.time.LocalDateTime;

public record ComentarioResponse(
        Long id,
        Long chamadoId,
        UsuarioResponse autor,
        String mensagem,
        boolean interno,
        LocalDateTime dataCriacao
) {
    public static ComentarioResponse de(ComentarioChamado c) {
        return new ComentarioResponse(
                c.getId(),
                c.getChamado().getId(),
                UsuarioResponse.de(c.getAutor()),
                c.getMensagem(),
                c.isInterno(),
                c.getDataCriacao()
        );
    }
}
