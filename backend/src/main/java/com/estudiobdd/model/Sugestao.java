package com.estudiobdd.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "sugestao")
public class Sugestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaUsuario historia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por_id", nullable = false)
    private Usuario registradaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decidida_por_id")
    private Usuario decididaPor;

    /** Nome do assistente de IA usado. */
    @Column(nullable = false, length = 120)
    private String ferramenta;

    @Column(name = "prompt_utilizado", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DecisaoSugestao decisao = DecisaoSugestao.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_rejeicao", length = 20)
    private MotivoRejeicao motivoRejeicao;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    protected Sugestao() {
    }

    public Sugestao(HistoriaUsuario historia, Usuario registradaPor, String ferramenta,
                    String prompt, String conteudo) {
        this.historia = historia;
        this.registradaPor = registradaPor;
        this.ferramenta = ferramenta;
        this.prompt = prompt;
        this.conteudo = conteudo;
        this.decisao = DecisaoSugestao.PENDENTE;
        this.registradoEm = LocalDateTime.now();
    }

    public boolean estaPendente() {
        return decisao == DecisaoSugestao.PENDENTE;
    }

    public void aceitar(Usuario decisor, String justificativa) {
        this.decisao = DecisaoSugestao.ACEITA;
        this.decididaPor = decisor;
        this.justificativa = justificativa;
        this.decididoEm = LocalDateTime.now();
    }

    public void rejeitar(Usuario decisor, MotivoRejeicao motivo, String justificativa) {
        this.decisao = DecisaoSugestao.REJEITADA;
        this.decididaPor = decisor;
        this.motivoRejeicao = motivo;
        this.justificativa = justificativa;
        this.decididoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public HistoriaUsuario getHistoria() {
        return historia;
    }

    public Usuario getRegistradaPor() {
        return registradaPor;
    }

    public Usuario getDecididaPor() {
        return decididaPor;
    }

    public String getFerramenta() {
        return ferramenta;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getConteudo() {
        return conteudo;
    }

    public DecisaoSugestao getDecisao() {
        return decisao;
    }

    public MotivoRejeicao getMotivoRejeicao() {
        return motivoRejeicao;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    public LocalDateTime getDecididoEm() {
        return decididoEm;
    }
}
