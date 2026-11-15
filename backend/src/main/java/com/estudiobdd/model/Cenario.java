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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cenario",
        uniqueConstraints = @UniqueConstraint(name = "uk_cenario_historia_titulo",
                columnNames = {"historia_id", "titulo"}))
public class Cenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaUsuario historia;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private int posicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OrigemCenario origem = OrigemCenario.HUMANO;

    /** Composição: os passos são removidos junto com o cenário. */
    @OneToMany(mappedBy = "cenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC")
    private List<Passo> passos = new ArrayList<>();

    protected Cenario() {
    }

    public Cenario(String titulo, OrigemCenario origem) {
        this.titulo = titulo;
        this.origem = origem == null ? OrigemCenario.HUMANO : origem;
    }

    public void adicionarPasso(Passo passo) {
        passo.setCenario(this);
        passos.add(passo);
    }

    /** Insere o passo na posição (1..n); sem posição, vai para o final. */
    public void inserirPasso(Passo passo, Integer posicao) {
        passo.setCenario(this);
        int indice = posicao == null ? passos.size()
                : Math.min(Math.max(posicao, 1), passos.size() + 1) - 1;
        passos.add(indice, passo);
        renumerarPassos();
    }

    public void removerPasso(Passo passo) {
        passos.remove(passo);
        renumerarPassos();
    }

    public void moverPasso(Passo passo, int posicao) {
        passos.remove(passo);
        int indice = Math.min(Math.max(posicao, 1), passos.size() + 1) - 1;
        passos.add(indice, passo);
        renumerarPassos();
    }

    /** Substitui todos os passos (os antigos são removidos por orphanRemoval). */
    public void substituirPassos(List<Passo> novos) {
        passos.clear();
        for (Passo p : novos) {
            p.setCenario(this);
            passos.add(p);
        }
        renumerarPassos();
    }

    /** Reatribui as posições 1..n conforme a ordem atual da lista. */
    public void renumerarPassos() {
        for (int i = 0; i < passos.size(); i++) {
            passos.get(i).setPosicao(i + 1);
        }
    }

    public Long getId() {
        return id;
    }

    public HistoriaUsuario getHistoria() {
        return historia;
    }

    void setHistoria(HistoriaUsuario historia) {
        this.historia = historia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getPosicao() {
        return posicao;
    }

    void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public OrigemCenario getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemCenario origem) {
        this.origem = origem;
    }

    public List<Passo> getPassos() {
        return passos;
    }
}
