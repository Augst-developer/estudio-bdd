package com.estudiobdd.exception;

/** HTTP 409: valor duplicado ou estado que não permite a operação (ex.: sugestão já decidida). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
