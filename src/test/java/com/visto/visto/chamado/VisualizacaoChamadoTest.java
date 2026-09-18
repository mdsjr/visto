package com.visto.visto.chamado;

import com.visto.visto.domain.chamado.VisualizacaoChamado;
import com.visto.visto.domain.usuario.PerfilUsuario;
import com.visto.visto.support.IntegracaoTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Alerta de dois técnicos acessando o mesmo chamado ao mesmo tempo. */
class VisualizacaoChamadoTest extends IntegracaoTestBase {

    private long abrirChamado(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/chamados")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "Impressora parou", "descricao", "Fila travada"))))
                .andExpect(status().isCreated())
                .andReturn();
        return lerJson(result).get("id").asLong();
    }

    @Test
    void primeiroTecnicoNaoRecebeAlerta() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String tec = registrar("Carlos Silva", "carlos@visto.dev", "senha123", PerfilUsuario.TECNICO);
        long id = abrirChamado(user);

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(tec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertaAtendimentoSimultaneo").value(false))
                .andExpect(jsonPath("$.outrosTecnicosVisualizando.length()").value(0));
    }

    @Test
    void segundoTecnicoRecebeAlertaComNomeDoPrimeiroEViceVersa() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String carlos = registrar("Carlos Silva", "carlos@visto.dev", "senha123", PerfilUsuario.TECNICO);
        String ana = registrar("Ana Souza", "ana@visto.dev", "senha123", PerfilUsuario.TECNICO);
        long id = abrirChamado(user);

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(carlos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertaAtendimentoSimultaneo").value(false));

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertaAtendimentoSimultaneo").value(true))
                .andExpect(jsonPath("$.outrosTecnicosVisualizando[0]").value("Carlos Silva"));

        // Carlos volta ao chamado: agora é ele quem vê a Ana
        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(carlos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertaAtendimentoSimultaneo").value(true))
                .andExpect(jsonPath("$.outrosTecnicosVisualizando.length()").value(1))
                .andExpect(jsonPath("$.outrosTecnicosVisualizando[0]").value("Ana Souza"));
    }

    @Test
    void tecnicoQueSaiDoChamadoDeixaDeGerarAlerta() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String carlos = registrar("Carlos Silva", "carlos@visto.dev", "senha123", PerfilUsuario.TECNICO);
        String ana = registrar("Ana Souza", "ana@visto.dev", "senha123", PerfilUsuario.TECNICO);
        long id = abrirChamado(user);

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(carlos))).andExpect(status().isOk());
        mockMvc.perform(delete("/chamados/" + id + "/visualizacao").header("Authorization", bearer(carlos)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertaAtendimentoSimultaneo").value(false));
    }

    @Test
    void visualizacaoAntigaForaDaJanelaNaoGeraAlerta() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String carlos = registrar("Carlos Silva", "carlos@visto.dev", "senha123", PerfilUsuario.TECNICO);
        String ana = registrar("Ana Souza", "ana@visto.dev", "senha123", PerfilUsuario.TECNICO);
        long id = abrirChamado(user);

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(carlos))).andExpect(status().isOk());

        // Simula que o Carlos acessou há 30 minutos (janela padrão é 5)
        VisualizacaoChamado v = visualizacaoRepository.findAll().get(0);
        v.setUltimaAtividade(LocalDateTime.now().minusMinutes(30));
        visualizacaoRepository.save(v);

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertaAtendimentoSimultaneo").value(false));
    }

    @Test
    void solicitanteNaoContaComoVisualizacaoNemRecebeAlerta() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String carlos = registrar("Carlos Silva", "carlos@visto.dev", "senha123", PerfilUsuario.TECNICO);
        long id = abrirChamado(user);

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(carlos))).andExpect(status().isOk());

        mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertaAtendimentoSimultaneo").value(false))
                .andExpect(jsonPath("$.outrosTecnicosVisualizando.length()").value(0));

        assertThat(visualizacaoRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/chamados/" + id + "/visualizacao").header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void acessoRepetidoDoMesmoTecnicoNaoDuplicaRegistro() throws Exception {
        String user = registrarUsuario("user@visto.dev");
        String carlos = registrarTecnico("carlos@visto.dev");
        long id = abrirChamado(user);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/chamados/" + id).header("Authorization", bearer(carlos))).andExpect(status().isOk());
        }

        assertThat(visualizacaoRepository.count()).isEqualTo(1);
    }
}
