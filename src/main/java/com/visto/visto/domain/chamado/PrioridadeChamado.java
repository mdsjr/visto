package com.visto.visto.domain.chamado;

/** Ordem declarada = ordem de criticidade (usada na fila). */
public enum PrioridadeChamado {
    BAIXA(72),
    MEDIA(24),
    ALTA(8),
    CRITICA(2);

    /** SLA padrão em horas para primeira resposta/resolução. */
    private final int slaHoras;

    PrioridadeChamado(int slaHoras) {
        this.slaHoras = slaHoras;
    }

    public int getSlaHoras() {
        return slaHoras;
    }
}
