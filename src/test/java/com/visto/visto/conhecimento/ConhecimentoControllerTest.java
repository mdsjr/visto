package com.visto.visto.conhecimento;

import com.fasterxml.jackson.databind.JsonNode;
import com.visto.visto.support.IntegracaoTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConhecimentoControllerTest extends IntegracaoTestBase {

    private Map<String, Object> artigo(String titulo, String problema, String tags) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("titulo", titulo);
        corpo.put("problema", problema);
        corpo.put("cenario", "Windows 11, notebook corporativo");
        corpo.put("itensAvaliados", "Cabo, driver, DNS");
        corpo.put("procedimento", "1. Reiniciar o adaptador\n2. Limpar cache DNS");
        if (tags != null) {
            corpo.put("tags", tags);
        }
        return corpo;
    }

    private JsonNode criar(String token, Map<String, Object> corpo) throws Exception {
        MvcResult result = mockMvc.perform(post("/conhecimento")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpo)))
                .andExpect(status().isCreated())
                .andReturn();
        return lerJson(result);
    }

    @Test
    void tecnicoCriaArtigoComTodosOsCampos() throws Exception {
        String tec = registrarTecnico("tec@visto.dev");

        JsonNode criado = criar(tec, artigo("VPN não conecta", "Erro 809 ao conectar VPN", "vpn, rede"));

        assertThat(criado.get("titulo").asText()).isEqualTo("VPN não conecta");
        assertThat(criado.get("autor").get("email").asText()).isEqualTo("tec@visto.dev");
        assertThat(criado.get("procedimento").asText()).contains("Reiniciar");
        assertThat(criado.get("chamadoOrigemId").isNull()).isTrue();
    }

    @Test
    void usuarioComumNaoPodeCriarArtigoMasPodeLer() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        criar(tec, artigo("Artigo 1", "Problema 1", null));

        mockMvc.perform(post("/conhecimento")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(artigo("Tentativa", "x", null))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/conhecimento").header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void buscaTextualEncontraPorTituloProblemaOuTags() throws Exception {
        String tec = registrarTecnico("tec@visto.dev");
        criar(tec, artigo("VPN não conecta", "Erro 809", "rede"));
        criar(tec, artigo("Impressora offline", "Fila travada no spooler", "impressao"));
        criar(tec, artigo("Outlook lento", "Perfil corrompido", "email, office"));

        mockMvc.perform(get("/conhecimento").param("busca", "vpn").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("VPN não conecta"));

        mockMvc.perform(get("/conhecimento").param("busca", "SPOOLER").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Impressora offline"));

        mockMvc.perform(get("/conhecimento").param("busca", "office").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Outlook lento"));

        mockMvc.perform(get("/conhecimento").param("busca", "nada-a-ver").header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void artigoVinculadoAChamadoDeOrigem() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrarTecnico("tec@visto.dev");
        MvcResult chamado = mockMvc.perform(post("/chamados")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "VPN", "descricao", "não conecta"))))
                .andExpect(status().isCreated())
                .andReturn();
        long chamadoId = lerJson(chamado).get("id").asLong();

        Map<String, Object> corpo = artigo("Solução VPN", "Erro 809", null);
        corpo.put("chamadoOrigemId", chamadoId);
        JsonNode criado = criar(tec, corpo);

        assertThat(criado.get("chamadoOrigemId").asLong()).isEqualTo(chamadoId);
    }

    @Test
    void artigoComChamadoInexistenteRetorna404() throws Exception {
        String tec = registrarTecnico("tec@visto.dev");
        Map<String, Object> corpo = artigo("Solução", "Problema", null);
        corpo.put("chamadoOrigemId", 999999L);

        mockMvc.perform(post("/conhecimento")
                        .header("Authorization", bearer(tec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void somenteAutorOuAdminEditaEExclui() throws Exception {
        String autor = registrarTecnico("autor@visto.dev");
        String outroTec = registrarTecnico("outro@visto.dev");
        String admin = registrarAdmin("admin@visto.dev");
        long id = criar(autor, artigo("Original", "Problema", null)).get("id").asLong();

        Map<String, Object> editado = artigo("Editado", "Problema", null);

        mockMvc.perform(put("/conhecimento/" + id)
                        .header("Authorization", bearer(outroTec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(editado)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/conhecimento/" + id)
                        .header("Authorization", bearer(autor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(editado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Editado"));

        mockMvc.perform(delete("/conhecimento/" + id).header("Authorization", bearer(outroTec)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/conhecimento/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/conhecimento/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void artigoSemProcedimentoRetorna400() throws Exception {
        String tec = registrarTecnico("tec@visto.dev");

        mockMvc.perform(post("/conhecimento")
                        .header("Authorization", bearer(tec))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "Só título", "problema", "x"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.procedimento").isNotEmpty());
    }
}
