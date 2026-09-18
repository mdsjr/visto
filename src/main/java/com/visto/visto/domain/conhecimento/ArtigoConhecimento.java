package com.visto.visto.domain.conhecimento;

import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Artigo da base de conhecimento. Cada chamado resolvido vira um ativo:
 * problema, cenário, itens avaliados e procedimento — para que qualquer técnico
 * resolva sozinho da próxima vez.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "artigos_conhecimento")
public class ArtigoConhecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    /** Sintoma/erro reportado (resumo pesquisável). */
    @Column(nullable = false, length = 2000)
    private String problema;

    /** Onde acontece: módulo, versão, ambiente, condições (pesquisável). */
    @Column(length = 2000)
    private String cenario;

    /** O que foi verificado até chegar na causa. */
    @Column(name = "itens_avaliados", columnDefinition = "TEXT")
    private String itensAvaliados;

    /** Passo a passo da solução. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String procedimento;

    /** Palavras-chave separadas por vírgula, para busca. */
    @Column(length = 500)
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    /** Chamado que deu origem ao artigo (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_origem_id")
    private Chamado chamadoOrigem;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = this.dataCriacao;
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
