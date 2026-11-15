package com.estudiobdd.exception;

/** HTTP 403: usuário identificado, mas sem permissão (não é o autor). */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
