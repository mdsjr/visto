package com.visto.visto.chamado;

import com.fasterxml.jackson.databind.JsonNode;
import com.visto.visto.support.IntegracaoTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChamadoControllerTest extends IntegracaoTestBase {

    private JsonNode abrirChamado(String token, String titulo, String prioridade) throws Exception {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("titulo", titulo);
        corpo.put("descricao", "Descrição do problema " + titulo);
        if (prioridade != null) {
            corpo.put("prioridade", prioridade);
        }
        MvcResult result = mockMvc.perform(post("/chamados")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpo)))
                .andExpect(status().isCreated())
                .andReturn();
        return lerJson(result);
    }

    @Test
    void usuarioAbreChamadoComStatusAbertoEPrioridadeMediaPorPadrao() throws Exception {
        String token = registrarUsuario("user@visto.dev");

        JsonNode chamado = abrirChamado(token, "Impressora não imprime", null);

        assertThat(chamado.get("status").asText()).isEqualTo("ABERTO");
        assertThat(chamado.get("prioridade").asText()).isEqualTo("MEDIA");
        assertThat(chamado.get("codigoPublico").asText()).hasSize(36);
        assertThat(chamado.get("solicitante").get("email").asText()).isEqualTo("user@visto.dev");
        assertThat(chamado.get("tecnico").isNull()).isTrue();
        assertThat(chamado.get("prazoSla").isNull()).isFalse();
    }

    @Test
    void abrirChamadoSemTituloRetorna400() throws Exception {
        String token = registrarUsuario("user@visto.dev");

        mockMvc.perform(post("/chamados")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", "só descrição"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").isNotEmpty());
    }

    @Test
    void usuarioSoEnxergaOsPropriosChamados() throws Exception {
        String ana = registrarUsuario("ana@visto.dev");
        String bruno = registrarUsuario("bruno@visto.dev");
        abrirChamado(ana, "Chamado da Ana", null);
        JsonNode doBruno = abrirChamado(bruno, "Chamado do Bruno", null);

        mockMvc.perform(get("/chamados").header("Authorization", bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Chamado da Ana"));

        // Ana não vê o chamado do Bruno nem por id (404 para não revelar existência)
        mockMvc.perform(get("/chamados/" + doBruno.get("id").asLong()).header("Authorization", bearer(ana)))
                .andExpect(status().isNotFound());
    }

    @Test
    void tecnicoVeFilaOrdenadaPorCriticidade() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        abrirChamado(user, "Baixa", "BAIXA");
        abrirChamado(user, "Crítica", "CRITICA");
        abrirChamado(user, "Alta", "ALTA");

        mockMvc.perform(get("/chamados").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].titulo").value("Crítica"))
                .andExpect(jsonPath("$.content[1].titulo").value("Alta"))
                .andExpect(jsonPath("$.content[2].titulo").value("Baixa"));

        mockMvc.perform(get("/chamados").param("prioridade", "ALTA").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Alta"));
    }

    @Test
    void tecnicoAssumeChamadoEStatusVaiParaEmAtendimento() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        long id = abrirChamado(user, "Rede caiu", "ALTA").get("id").asLong();

        mockMvc.perform(patch("/chamados/" + id + "/assumir").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.tecnico.email").value("tec@visto.dev"));
    }

    @Test
    void segundoTecnicoNaoConsegueAssumirChamadoJaAssumido() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec1 = registrarTecnico("tec1@visto.dev");
        String tec2 = registrarTecnico("tec2@visto.dev");
        long id = abrirChamado(user, "Rede caiu", null).get("id").asLong();

        mockMvc.perform(patch("/chamados/" + id + "/assumir").header("Authorization", bearer(tec1)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/chamados/" + id + "/assumir").header("Authorization", bearer(tec2)))
                .andExpect(status().isConflict());
    }

    @Test
    void usuarioComumNaoPodeAssumirChamado() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        long id = abrirChamado(user, "Teste", null).get("id").asLong();

        mockMvc.perform(patch("/chamados/" + id + "/assumir").header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fluxoDeStatusRespeitaTransicoes() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        long id = abrirChamado(user, "Erro no sistema", null).get("id").asLong();

        // ABERTO -> RESOLVIDO não é permitido
        mockMvc.perform(patch("/chamados/" + id + "/status")
                        .header("Authorization", bearer(tec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "RESOLVIDO"))))
                .andExpect(status().isConflict());

        // ABERTO -> EM_ATENDIMENTO (técnico vira responsável automaticamente)
        mockMvc.perform(patch("/chamados/" + id + "/status")
                        .header("Authorization", bearer(tec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "EM_ATENDIMENTO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnico.email").value("tec@visto.dev"));

        // EM_ATENDIMENTO -> RESOLVIDO preenche dataResolucao
        mockMvc.perform(patch("/chamados/" + id + "/status")
                        .header("Authorization", bearer(tec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "RESOLVIDO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDO"))
                .andExpect(jsonPath("$.dataResolucao").isNotEmpty());

        // Solicitante pode FECHAR o próprio chamado
        mockMvc.perform(patch("/chamados/" + id + "/status")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "FECHADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADO"));
    }

    @Test
    void solicitanteNaoPodeMudarStatusParaOutroQueNaoSejaFechado() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        long id = abrirChamado(user, "Erro", null).get("id").asLong();

        mockMvc.perform(patch("/chamados/" + id + "/status")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "EM_ATENDIMENTO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void alterarPrioridadeRecalculaSla() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        JsonNode chamado = abrirChamado(user, "Lento", "BAIXA");
        long id = chamado.get("id").asLong();
        String prazoAntes = chamado.get("prazoSla").asText();

        MvcResult result = mockMvc.perform(patch("/chamados/" + id + "/prioridade")
                        .header("Authorization", bearer(tec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("prioridade", "CRITICA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridade").value("CRITICA"))
                .andReturn();

        String prazoDepois = lerJson(result).get("prazoSla").asText();
        assertThat(prazoDepois).isLessThan(prazoAntes);
    }

    @Test
    void acompanhamentoPublicoFuncionaSemLoginENaoExpoeSolicitante() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        JsonNode chamado = abrirChamado(user, "Sem internet", null);
        String codigo = chamado.get("codigoPublico").asText();
        mockMvc.perform(patch("/chamados/" + chamado.get("id").asLong() + "/assumir")
                        .header("Authorization", bearer(tec)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/public/chamados/" + codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Sem internet"))
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.tecnico").value("Técnico"))
                .andExpect(jsonPath("$.solicitante").doesNotExist())
                .andExpect(jsonPath("$.descricao").doesNotExist());
    }

    @Test
    void acompanhamentoPublicoComCodigoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/public/chamados/nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void chamadoInexistenteRetorna404() throws Exception {
        String tec = registrarTecnico("tec@visto.dev");

        mockMvc.perform(get("/chamados/999999").header("Authorization", bearer(tec)))
                .andExpect(status().isNotFound());
    }
}
