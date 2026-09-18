package com.visto.visto.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
    public static ErroResponse de(int status, String erro, String mensagem, String caminho) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, caminho, null);
    }

    public static ErroResponse validacao(String mensagem, String caminho, Map<String, String> campos) {
        return new ErroResponse(LocalDateTime.now(), 400, "Bad Request", mensagem, caminho, campos);
    }
}
