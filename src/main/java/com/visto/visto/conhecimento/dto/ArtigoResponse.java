package com.visto.visto.conhecimento.dto;

import com.visto.visto.auth.dto.UsuarioResponse;
import com.visto.visto.domain.conhecimento.ArtigoConhecimento;

import java.time.LocalDateTime;

public record ArtigoResponse(
        Long id,
        String titulo,
        String problema,
        String cenario,
        String itensAvaliados,
        String procedimento,
        String tags,
        UsuarioResponse autor,
        Long chamadoOrigemId,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
    public static ArtigoResponse de(ArtigoConhecimento a) {
        return new ArtigoResponse(
                a.getId(),
                a.getTitulo(),
                a.getProblema(),
                a.getCenario(),
                a.getItensAvaliados(),
                a.getProcedimento(),
                a.getTags(),
                UsuarioResponse.de(a.getAutor()),
                a.getChamadoOrigem() != null ? a.getChamadoOrigem().getId() : null,
                a.getDataCriacao(),
                a.getDataAtualizacao()
        );
    }
}
