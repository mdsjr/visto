package com.visto.visto.exception;

/** Violação de regra de negócio (ex.: e-mail duplicado, transição de status inválida). Responde 409. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
