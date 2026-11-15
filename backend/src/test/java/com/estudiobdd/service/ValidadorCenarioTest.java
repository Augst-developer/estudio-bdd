package com.estudiobdd.service;

import com.estudiobdd.exception.RegraDeNegocioException;
import com.estudiobdd.model.TipoPasso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.estudiobdd.model.TipoPasso.DADO;
import static com.estudiobdd.model.TipoPasso.E;
import static com.estudiobdd.model.TipoPasso.ENTAO;
import static com.estudiobdd.model.TipoPasso.MAS;
import static com.estudiobdd.model.TipoPasso.QUANDO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorCenarioTest {

    private static String erro(TipoPasso... tipos) {
        return assertThrows(RegraDeNegocioException.class,
                () -> ValidadorCenario.validar(List.of(tipos))).getMessage();
    }

    @Test
    void aceitaSequenciaValidaComContinuacoes() {
        assertDoesNotThrow(() -> ValidadorCenario.validar(List.of(DADO, E, QUANDO, ENTAO, E, MAS)));
        assertDoesNotThrow(() -> ValidadorCenario.validar(List.of(QUANDO, ENTAO)));
    }

    @Test
    void rejeitaCenarioSemEntao() {
        assertTrue(erro(DADO, QUANDO).contains("Então"));
    }

    @Test
    void rejeitaCenarioSemQuando() {
        assertTrue(erro(DADO, ENTAO).contains("Quando"));
    }

    @Test
    void rejeitaPassosForaDeOrdem() {
        assertTrue(erro(QUANDO, DADO, ENTAO).contains("fora de ordem"));
        assertTrue(erro(DADO, ENTAO, QUANDO).contains("fora de ordem"));
    }

    @Test
    void rejeitaCenarioIniciadoPorEOuMas() {
        assertTrue(erro(E, QUANDO, ENTAO).contains("começar"));
        assertTrue(erro(MAS, QUANDO, ENTAO).contains("começar"));
    }

    @Test
    void rejeitaCenarioSemPassos() {
        assertThrows(RegraDeNegocioException.class, () -> ValidadorCenario.validar(List.of()));
    }
}
