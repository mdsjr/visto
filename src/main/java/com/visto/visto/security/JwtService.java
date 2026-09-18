package com.visto.visto.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Geração e validação de tokens JWT (HS256) usando JJWT 0.12.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMillis;

    public JwtService(@Value("${visto.jwt.secret}") String secret,
                      @Value("${visto.jwt.expiracao-ms:86400000}") long expiracaoMillis) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMillis = expiracaoMillis;
    }

    public String gerarToken(UserDetails usuario) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoMillis))
                .signWith(chave)
                .compact();
    }

    /**
     * Retorna o e-mail (subject) contido no token, ou vazio se o token for inválido/expirado.
     */
    public Optional<String> extrairEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean tokenValido(String token, UserDetails usuario) {
        return extrairEmail(token)
                .map(email -> email.equals(usuario.getUsername()))
                .orElse(false);
    }

    public long getExpiracaoMillis() {
        return expiracaoMillis;
    }
}
