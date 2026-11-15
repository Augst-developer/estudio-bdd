package com.estudiobdd.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "projeto",
        uniqueConstraints = @UniqueConstraint(name = "uk_projeto_nome", columnNames = "nome"))
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    /** Glossário e informações do negócio. */
    @Column(name = "contexto_dominio", columnDefinition = "TEXT")
    private String contextoDominio;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    protected Projeto() {
    }

    public Projeto(String nome, String descricao, String contextoDominio) {
        this.nome = nome;
        this.descricao = descricao;
        this.contextoDominio = contextoDominio;
    }

    @PrePersist
    void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
    }

    public void atualizar(String nome, String descricao, String contextoDominio) {
        this.nome = nome;
        this.descricao = descricao;
        this.contextoDominio = contextoDominio;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getContextoDominio() {
        return contextoDominio;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}
