package com.visto.visto.domain.chamado;

import com.visto.visto.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chamados", indexes = {
        @Index(name = "idx_chamado_codigo_publico", columnList = "codigo_publico", unique = true),
        @Index(name = "idx_chamado_status", columnList = "status")
})
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código usado no link público de acompanhamento (sem expor o id sequencial). */
    @Column(name = "codigo_publico", nullable = false, updatable = false, length = 36)
    private String codigoPublico;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusChamado status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeChamado prioridade;

    /** Cópia numérica da prioridade (ordinal) para ordenar a fila no banco. Mantida automaticamente. */
    @Column(name = "nivel_prioridade", nullable = false)
    private int nivelPrioridade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

    @Column(name = "prazo_sla")
    private LocalDateTime prazoSla;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        this.dataAbertura = agora;
        this.dataAtualizacao = agora;
        if (this.codigoPublico == null) {
            this.codigoPublico = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = StatusChamado.ABERTO;
        }
        if (this.prioridade == null) {
            this.prioridade = PrioridadeChamado.MEDIA;
        }
        this.nivelPrioridade = this.prioridade.ordinal();
        this.prazoSla = agora.plusHours(this.prioridade.getSlaHoras());
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
        if (this.prioridade != null) {
            this.nivelPrioridade = this.prioridade.ordinal();
        }
    }

    public boolean isSolicitante(Usuario usuario) {
        return usuario != null && solicitante != null && solicitante.getId().equals(usuario.getId());
    }

    public boolean isTecnicoResponsavel(Usuario usuario) {
        return usuario != null && tecnico != null && tecnico.getId().equals(usuario.getId());
    }

    public boolean isSlaEstourado() {
        return !status.isEncerrado() && prazoSla != null && LocalDateTime.now().isAfter(prazoSla);
    }
}
