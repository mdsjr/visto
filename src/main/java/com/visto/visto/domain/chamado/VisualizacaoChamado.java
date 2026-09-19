package com.visto.visto.domain.chamado;

import com.visto.visto.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de "técnico com o chamado aberto". Serve para avisar quando dois técnicos
 * estão olhando o mesmo chamado ao mesmo tempo. Uma linha por (chamado, técnico),
 * renovada a cada acesso; considerada ativa dentro da janela configurada.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "visualizacoes_chamado", uniqueConstraints = {
        @UniqueConstraint(name = "uk_visualizacao_chamado_tecnico", columnNames = {"chamado_id", "tecnico_id"})
})
public class VisualizacaoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Usuario tecnico;

    @Column(name = "ultima_atividade", nullable = false)
    private LocalDateTime ultimaAtividade;
}
