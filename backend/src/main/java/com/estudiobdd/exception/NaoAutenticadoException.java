package com.estudiobdd.exception;

/** HTTP 401: cabeçalho X-Usuario-Id ausente ou com usuário inexistente. */
public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
