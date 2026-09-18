package com.visto.visto.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visto.visto.domain.usuario.PerfilUsuario;
import com.visto.visto.repository.ArtigoConhecimentoRepository;
import com.visto.visto.repository.ChamadoRepository;
import com.visto.visto.repository.ComentarioChamadoRepository;
import com.visto.visto.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração: sobe o contexto com H2 (profile test), limpa o banco
 * antes de cada teste e oferece atalhos para cadastrar usuários e obter tokens.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegracaoTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected ChamadoRepository chamadoRepository;

    @Autowired
    protected ArtigoConhecimentoRepository artigoRepository;

    @Autowired
    protected ComentarioChamadoRepository comentarioRepository;

    @BeforeEach
    void limparBanco() {
        artigoRepository.deleteAll();
        comentarioRepository.deleteAll();
        chamadoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    protected String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    protected JsonNode lerJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /** Cadastra um usuário via /auth/register e devolve o token JWT. */
    protected String registrar(String nome, String email, String senha, PerfilUsuario perfil) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", nome,
                                "email", email,
                                "senha", senha,
                                "perfil", perfil.name()))))
                .andExpect(status().isCreated())
                .andReturn();
        return lerJson(result).get("token").asText();
    }

    protected String registrarUsuario(String email) throws Exception {
        return registrar("Usuário Teste", email, "senha123", PerfilUsuario.USUARIO);
    }

    protected String registrarTecnico(String email) throws Exception {
        return registrar("Técnico Teste", email, "senha123", PerfilUsuario.TECNICO);
    }

    protected String registrarAdmin(String email) throws Exception {
        return registrar("Admin Teste", email, "senha123", PerfilUsuario.ADMIN);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
