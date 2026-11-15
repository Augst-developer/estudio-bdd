package com.estudiobdd.exception;

/** HTTP 422: a requisição é válida sintaticamente, mas viola uma regra de negócio. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
