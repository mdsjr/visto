package com.visto.visto.chamado.dto;

import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.chamado.PrioridadeChamado;
import com.visto.visto.domain.chamado.StatusChamado;

import java.time.LocalDateTime;

/**
 * Visão pública do chamado (link de acompanhamento): sem dados pessoais do solicitante,
 * só o primeiro nome do técnico.
 */
public record AcompanhamentoPublicoResponse(
        String codigoPublico,
        String titulo,
        StatusChamado status,
        PrioridadeChamado prioridade,
        String tecnico,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtualizacao,
        LocalDateTime dataResolucao
) {
    public static AcompanhamentoPublicoResponse de(Chamado c) {
        String tecnico = null;
        if (c.getTecnico() != null && c.getTecnico().getNome() != null) {
            tecnico = c.getTecnico().getNome().split(" ")[0];
        }
        return new AcompanhamentoPublicoResponse(
                c.getCodigoPublico(),
                c.getTitulo(),
                c.getStatus(),
                c.getPrioridade(),
                tecnico,
                c.getDataAbertura(),
                c.getDataAtualizacao(),
                c.getDataResolucao()
        );
    }
}
