package com.estudiobdd.service;

import com.estudiobdd.exception.RegraDeNegocioException;
import com.estudiobdd.model.TipoPasso;

import java.util.List;

/**
 * Regra 2: um cenário válido tem ao menos um Quando e um Então e segue a ordem
 * Dado → Quando → Então. E e Mas continuam o tipo anterior e não podem iniciar o cenário.
 * Classe sem dependências do Spring, para ser testada isoladamente.
 */
public final class ValidadorCenario {

    private ValidadorCenario() {
    }

    public static void validar(List<TipoPasso> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            throw new RegraDeNegocioException("O cenário deve ter ao menos um passo.");
        }
        if (tipos.get(0).ehContinuacao()) {
            throw new RegraDeNegocioException("O cenário não pode começar com um passo "
                    + tipos.get(0).palavraChave() + ": use Dado ou Quando.");
        }

        int nivelAtual = -1;
        boolean temQuando = false;
        boolean temEntao = false;
        for (int i = 0; i < tipos.size(); i++) {
            TipoPasso tipo = tipos.get(i);
            int nivel = tipo.ehContinuacao() ? nivelAtual : nivel(tipo);
            if (nivel < nivelAtual) {
                throw new RegraDeNegocioException("Passos fora de ordem na posição " + (i + 1)
                        + ": a sequência deve ser Dado → Quando → Então.");
            }
            nivelAtual = nivel;
            temQuando |= tipo == TipoPasso.QUANDO;
            temEntao |= tipo == TipoPasso.ENTAO;
        }
        if (!temQuando) {
            throw new RegraDeNegocioException("O cenário deve ter ao menos um passo Quando.");
        }
        if (!temEntao) {
            throw new RegraDeNegocioException("O cenário deve ter ao menos um passo Então.");
        }
    }

    private static int nivel(TipoPasso tipo) {
        return switch (tipo) {
            case DADO -> 0;
            case QUANDO -> 1;
            case ENTAO -> 2;
            default -> throw new IllegalArgumentException("Tipo sem nível próprio: " + tipo);
        };
    }
}
