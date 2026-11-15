package com.estudiobdd.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Associações ManyToOne são LAZY (projeto, autor) para não carregar dados que a
 * operação não usa. As coleções (cenários e sugestões) são LAZY por padrão e
 * pertencem a esta entidade por composição (cascade + orphanRemoval).
 */
@Entity
@Table(name = "historia_usuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_historia_projeto_titulo",
                columnNames = {"projeto_id", "titulo"}))
public class HistoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, length = 200)
    private String titulo;

    /** Como... */
    @Column(nullable = false, length = 500)
    private String papel;

    /** quero... */
    @Column(nullable = false, length = 1000)
    private String objetivo;

    /** para... */
    @Column(nullable = false, length = 1000)
    private String beneficio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC")
    private List<Cenario> cenarios = new ArrayList<>();

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Sugestao> sugestoes = new ArrayList<>();

    protected HistoriaUsuario() {
    }

    public HistoriaUsuario(Projeto projeto, Usuario autor, String titulo, String papel,
                           String objetivo, String beneficio, Prioridade prioridade) {
        this.projeto = projeto;
        this.autor = autor;
        this.titulo = titulo;
        this.papel = papel;
        this.objetivo = objetivo;
        this.beneficio = beneficio;
        this.prioridade = prioridade;
    }

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /** Atualiza a data de modificação quando só os filhos (cenários/passos) mudaram. */
    public void tocar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public void atualizar(String titulo, String papel, String objetivo, String beneficio,
                          Prioridade prioridade) {
        this.titulo = titulo;
        this.papel = papel;
        this.objetivo = objetivo;
        this.beneficio = beneficio;
        this.prioridade = prioridade;
        tocar();
    }

    public boolean ehAutor(Usuario usuario) {
        return usuario != null && autor != null && autor.getId() != null
                && autor.getId().equals(usuario.getId());
    }

    // ---- gerenciamento da coleção ordenada de cenários ----

    /** Adiciona o cenário ao final da lista. */
    public void adicionarCenario(Cenario cenario) {
        cenario.setHistoria(this);
        cenarios.add(cenario);
        renumerarCenarios();
    }

    public void removerCenario(Cenario cenario) {
        cenarios.remove(cenario);
        renumerarCenarios();
    }

    /** Move o cenário para a posição informada (1..n), ajustando as demais. */
    public void moverCenario(Cenario cenario, int posicao) {
        cenarios.remove(cenario);
        int indice = Math.min(Math.max(posicao, 1), cenarios.size() + 1) - 1;
        cenarios.add(indice, cenario);
        renumerarCenarios();
    }

    private void renumerarCenarios() {
        for (int i = 0; i < cenarios.size(); i++) {
            cenarios.get(i).setPosicao(i + 1);
        }
    }

    public Long getId() {
        return id;
    }

    public Projeto getProjeto() {
        return projeto;
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getPapel() {
        return papel;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public String getBeneficio() {
        return beneficio;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public List<Cenario> getCenarios() {
        return cenarios;
    }

    public List<Sugestao> getSugestoes() {
        return sugestoes;
    }
}
