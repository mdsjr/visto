package com.visto.visto.security;

import com.visto.visto.domain.usuario.PerfilUsuario;
import com.visto.visto.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-somente-para-testes-0123456789abcdefghij";

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SEGREDO, 60_000L);
        usuario = Usuario.builder()
                .id(1L)
                .nome("Moacir")
                .email("moacir@visto.dev")
                .senha("hash")
                .perfil(PerfilUsuario.TECNICO)
                .build();
    }

    @Test
    void geraTokenComSubjectIgualAoEmail() {
        String token = jwtService.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairEmail(token)).contains("moacir@visto.dev");
        assertThat(jwtService.tokenValido(token, usuario)).isTrue();
    }

    @Test
    void tokenExpiradoEhInvalido() {
        JwtService expirado = new JwtService(SEGREDO, -1_000L);
        String token = expirado.gerarToken(usuario);

        assertThat(expirado.extrairEmail(token)).isEmpty();
        assertThat(expirado.tokenValido(token, usuario)).isFalse();
    }

    @Test
    void tokenAssinadoComOutroSegredoEhInvalido() {
        JwtService outro = new JwtService("outro-segredo-completamente-diferente-0123456789", 60_000L);
        String token = outro.gerarToken(usuario);

        assertThat(jwtService.extrairEmail(token)).isEmpty();
    }

    @Test
    void tokenMalformadoNaoLancaExcecao() {
        assertThat(jwtService.extrairEmail("isso-nao-e-um-jwt")).isEmpty();
        assertThat(jwtService.extrairEmail("")).isEmpty();
    }

    @Test
    void tokenDeOutroUsuarioNaoEhValidoParaEste() {
        Usuario outra = Usuario.builder().email("outra@visto.dev").perfil(PerfilUsuario.USUARIO).build();
        String token = jwtService.gerarToken(outra);

        assertThat(jwtService.tokenValido(token, usuario)).isFalse();
    }
}
