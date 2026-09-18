package com.visto.visto.auth;

import com.visto.visto.domain.usuario.PerfilUsuario;
import com.visto.visto.support.IntegracaoTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegracaoTestBase {

    @Test
    void registraUsuarioERetornaToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Moacir", "email", "Moacir@Visto.dev", "senha", "senha123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("moacir@visto.dev"))
                .andExpect(jsonPath("$.usuario.perfil").value("USUARIO"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void registroComEmailDuplicadoRetorna409() throws Exception {
        registrarUsuario("dup@visto.dev");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Outro", "email", "dup@visto.dev", "senha", "senha123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(startsWith("Já existe um usuário")));
    }

    @Test
    void registroComDadosInvalidosRetorna400ComCampos() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "", "email", "nao-e-email", "senha", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").isNotEmpty())
                .andExpect(jsonPath("$.campos.email").isNotEmpty())
                .andExpect(jsonPath("$.campos.senha").isNotEmpty());
    }

    @Test
    void loginComCredenciaisCorretasRetornaToken() throws Exception {
        registrar("Téc", "tec@visto.dev", "senha123", PerfilUsuario.TECNICO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "tec@visto.dev", "senha", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.perfil").value("TECNICO"));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        registrarUsuario("user@visto.dev");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "user@visto.dev", "senha", "errada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComEmailInexistenteRetorna401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "ninguem@visto.dev", "senha", "senha123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meRetornaUsuarioAutenticado() throws Exception {
        String token = registrarUsuario("me@visto.dev");

        mockMvc.perform(get("/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@visto.dev"));
    }

    @Test
    void rotaProtegidaSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaComTokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }
}
