package com.visto.visto.chamado.dto;

import com.visto.visto.auth.dto.UsuarioResponse;
import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.chamado.PrioridadeChamado;
import com.visto.visto.domain.chamado.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

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
        boolean slaEstourado,
        /** Nomes de outros técnicos que estão com este chamado aberto agora (só preenchido no detalhe). */
        List<String> outrosTecnicosVisualizando,
        /** true quando há outro técnico no chamado ao mesmo tempo. */
        boolean alertaAtendimentoSimultaneo
) {
    public static ChamadoResponse de(Chamado c) {
        return de(c, List.of());
    }

    public static ChamadoResponse de(Chamado c, List<String> outrosTecnicos) {
        List<String> outros = outrosTecnicos == null ? List.of() : outrosTecnicos;
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
                c.isSlaEstourado(),
                outros,
                !outros.isEmpty()
        );
    }
}
