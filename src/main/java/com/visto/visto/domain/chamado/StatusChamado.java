package com.visto.visto.domain.chamado;

import java.util.Set;

public enum StatusChamado {
    ABERTO,
    EM_ATENDIMENTO,
    AGUARDANDO_USUARIO,
    RESOLVIDO,
    FECHADO;

    /** Transições permitidas a partir de cada status. */
    public Set<StatusChamado> proximosPermitidos() {
        return switch (this) {
            case ABERTO -> Set.of(EM_ATENDIMENTO, FECHADO);
            case EM_ATENDIMENTO -> Set.of(AGUARDANDO_USUARIO, RESOLVIDO, FECHADO);
            case AGUARDANDO_USUARIO -> Set.of(EM_ATENDIMENTO, RESOLVIDO, FECHADO);
            case RESOLVIDO -> Set.of(EM_ATENDIMENTO, FECHADO);
            case FECHADO -> Set.of();
        };
    }

    public boolean podeIrPara(StatusChamado destino) {
        return proximosPermitidos().contains(destino);
    }

    public boolean isEncerrado() {
        return this == RESOLVIDO || this == FECHADO;
    }
}
