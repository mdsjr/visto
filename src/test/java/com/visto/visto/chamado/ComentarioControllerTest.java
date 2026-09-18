package com.visto.visto.chamado;

import com.visto.visto.support.IntegracaoTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ComentarioControllerTest extends IntegracaoTestBase {

    private long abrirChamado(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/chamados")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "Sem acesso ao sistema", "descricao", "Erro ao logar"))))
                .andExpect(status().isCreated())
                .andReturn();
        return lerJson(result).get("id").asLong();
    }

    private void comentar(String token, long chamadoId, String mensagem, boolean interno, int statusEsperado)
            throws Exception {
        mockMvc.perform(post("/chamados/" + chamadoId + "/comentarios")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("mensagem", mensagem, "interno", interno))))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void solicitanteETecnicoConversamNoChamado() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        long id = abrirChamado(user);

        mockMvc.perform(post("/chamados/" + id + "/comentarios")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("mensagem", "Já tentei reiniciar"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor.email").value("user@visto.dev"))
                .andExpect(jsonPath("$.interno").value(false))
                .andExpect(jsonPath("$.chamadoId").value(id));

        comentar(tec, id, "Vou verificar seu acesso no AD", false, 201);

        mockMvc.perform(get("/chamados/" + id + "/comentarios").header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].mensagem").value("Já tentei reiniciar"))
                .andExpect(jsonPath("$[1].autor.email").value("tec@visto.dev"));
    }

    @Test
    void notaInternaNaoApareceParaOSolicitante() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        long id = abrirChamado(user);

        comentar(tec, id, "Usuário está com a conta bloqueada por 3 tentativas", true, 201);
        comentar(tec, id, "Estou verificando, já retorno", false, 201);

        mockMvc.perform(get("/chamados/" + id + "/comentarios").header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].interno").value(false));

        mockMvc.perform(get("/chamados/" + id + "/comentarios").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].interno").value(true));
    }

    @Test
    void usuarioComumNaoPodeCriarNotaInterna() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        long id = abrirChamado(user);

        comentar(user, id, "tentando nota interna", true, 403);
    }

    @Test
    void usuarioNaoComentaEmChamadoDeOutro() throws Exception {
        String ana = registrarUsuario("ana@visto.dev");
        String bruno = registrarUsuario("bruno@visto.dev");
        long idDaAna = abrirChamado(ana);

        comentar(bruno, idDaAna, "intrometido", false, 404);

        mockMvc.perform(get("/chamados/" + idDaAna + "/comentarios").header("Authorization", bearer(bruno)))
                .andExpect(status().isNotFound());
    }

    @Test
    void chamadoFechadoNaoAceitaComentario() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        long id = abrirChamado(user);

        mockMvc.perform(patch("/chamados/" + id + "/status")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "FECHADO"))))
                .andExpect(status().isOk());

        comentar(user, id, "esqueci de falar", false, 409);
    }

    @Test
    void respostaDoSolicitanteReabreAtendimento() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        long id = abrirChamado(user);

        mockMvc.perform(patch("/chamados/" + id + "/assumir").header("Authorization", bearer(tec)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/chamados/" + id + "/status")
                        .header("Authorization", bearer(tec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "AGUARDANDO_USUARIO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_USUARIO"));

        comentar(user, id, "Segue a informação que pediu", false, 201);

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
    }

    @Test
    void mensagemVaziaRetorna400() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        long id = abrirChamado(user);

        mockMvc.perform(post("/chamados/" + id + "/comentarios")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("mensagem", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.mensagem").isNotEmpty());
    }
}
