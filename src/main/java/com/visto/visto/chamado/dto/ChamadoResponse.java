package com.visto.visto.chamado.dto;

import com.visto.visto.auth.dto.UsuarioResponse;
import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.chamado.PrioridadeChamado;
import com.visto.visto.domain.chamado.StatusChamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        String codigoPublico,
        String titulo,
        String descricao,
        StatusChamado status,
        PrioridadeChamado prioridade,
        UsuarioResponse solicitante,
        UsuarioResponse tecnico,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtualizacao,
        LocalDateTime dataResolucao,
        LocalDateTime prazoSla,
        boolean slaEstourado
) {
    public static ChamadoResponse de(Chamado c) {
        return new ChamadoResponse(
                c.getId(),
                c.getCodigoPublico(),
                c.getTitulo(),
                c.getDescricao(),
                c.getStatus(),
                c.getPrioridade(),
                UsuarioResponse.de(c.getSolicitante()),
                UsuarioResponse.de(c.getTecnico()),
                c.getDataAbertura(),
                c.getDataAtualizacao(),
                c.getDataResolucao(),
                c.getPrazoSla(),
                c.isSlaEstourado()
        );
    }
}
