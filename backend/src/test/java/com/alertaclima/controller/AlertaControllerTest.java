package com.alertaclima.controller;

import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.exception.RegraNegocioException;
import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.StatusAlerta;
import com.alertaclima.service.AlertaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertaController.class)
class AlertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertaService alertaService;

    private Alerta alerta;

    @BeforeEach
    void setUp() {
        alerta = new Alerta();
        alerta.setId("1");
        alerta.setTitulo("Alagamento severo");
        alerta.setStatus(StatusAlerta.SUSPEITO);
        alerta.setNivelPerigo("Alto");
    }

    @Test
    void criarRetorna201ComAlertaCriado() throws Exception {
        when(alertaService.criarAlerta(any(Alerta.class))).thenReturn(alerta);

        mockMvc.perform(post("/api/alertas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(alerta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Alerta registrado com sucesso."))
                .andExpect(jsonPath("$.alerta.id").value("1"));
    }

    @Test
    void listarAtivosRetornaAlertasAtivos() throws Exception {
        when(alertaService.buscarAlertasAtivos(null, null, null, null, null, null))
                .thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void listarArquivadosRetornaAlertasArquivados() throws Exception {
        when(alertaService.buscarAlertasArquivados()).thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/alertas/arquivados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void buscarPorIdRetorna200QuandoEncontrado() throws Exception {
        when(alertaService.buscarAlertaPorId("1")).thenReturn(alerta);

        mockMvc.perform(get("/api/alertas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Alagamento severo"));
    }

    @Test
    void buscarPorIdRetorna404QuandoNaoEncontrado() throws Exception {
        when(alertaService.buscarAlertaPorId("999")).thenThrow(new RecursoNaoEncontradoException("Alerta não encontrado"));

        mockMvc.perform(get("/api/alertas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Alerta não encontrado"));
    }

    @Test
    void listarConverteRotuloDoStatusParaEnum() throws Exception {
        when(alertaService.buscarAlertasAtivos(null, StatusAlerta.EM_ANALISE, null, null, null, null))
                .thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/alertas").param("status", "EM ANÁLISE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void listarComStatusInvalidoRetorna400() throws Exception {
        mockMvc.perform(get("/api/alertas").param("status", "QUALQUER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Status inválido")));
        verifyNoInteractions(alertaService);
    }

    @Test
    void statusEhSerializadoPeloRotulo() throws Exception {
        alerta.setStatus(StatusAlerta.EM_ANALISE);
        when(alertaService.buscarAlertaPorId("1")).thenReturn(alerta);

        mockMvc.perform(get("/api/alertas/1"))
                .andExpect(jsonPath("$.status").value("EM ANÁLISE"));
    }

    @Test
    void atualizarRetorna200QuandoEncontrado() throws Exception {
        when(alertaService.atualizarAlerta(eq("1"), any(Alerta.class))).thenReturn(alerta);

        mockMvc.perform(put("/api/alertas/1")
                        .contentType("application/json")
                        .content("{\"nivelPerigo\":\"Crítico\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarRetorna404QuandoNaoEncontrado() throws Exception {
        when(alertaService.atualizarAlerta(eq("999"), any(Alerta.class)))
                .thenThrow(new RecursoNaoEncontradoException("Alerta não encontrado"));

        mockMvc.perform(put("/api/alertas/999")
                        .contentType("application/json")
                        .content("{\"nivelPerigo\":\"Crítico\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarStatusParseiaValidadoPorERetorna200() throws Exception {
        when(alertaService.atualizarStatus(eq("1"), eq(StatusAlerta.CONFIRMADO), any(InformacoesUsuario.class), eq("ok")))
                .thenReturn(alerta);

        Map<String, Object> body = Map.of(
                "status", "CONFIRMADO",
                "notasValidacao", "ok",
                "validadoPor", Map.of("nome", "Analista", "email", "a@a.com", "papel", "ANALYST")
        );

        mockMvc.perform(patch("/api/alertas/1/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(alertaService).atualizarStatus(eq("1"), eq(StatusAlerta.CONFIRMADO),
                argThat(v -> v.getPapel() == PapelUsuario.ANALYST && "Analista".equals(v.getNome())), eq("ok"));
    }

    @Test
    void atualizarStatusAceitaRotuloComAcento() throws Exception {
        when(alertaService.atualizarStatus(eq("1"), eq(StatusAlerta.EM_ANALISE), isNull(), isNull()))
                .thenReturn(alerta);

        mockMvc.perform(patch("/api/alertas/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"EM ANÁLISE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarStatusComValorInvalidoRetorna400() throws Exception {
        mockMvc.perform(patch("/api/alertas/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"ARQUIVADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        verifyNoInteractions(alertaService);
    }

    @Test
    void atualizarStatusSemStatusRetorna400() throws Exception {
        when(alertaService.atualizarStatus(eq("1"), isNull(), any(), any()))
                .thenThrow(new RegraNegocioException("O status é obrigatório."));

        mockMvc.perform(patch("/api/alertas/1/status")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O status é obrigatório."));
    }

    @Test
    void atualizarStatusRetorna404QuandoNaoEncontrado() throws Exception {
        when(alertaService.atualizarStatus(eq("999"), any(), any(), any()))
                .thenThrow(new RecursoNaoEncontradoException("Alerta não encontrado"));

        mockMvc.perform(patch("/api/alertas/999/status")
                        .contentType("application/json")
                        .content("{\"status\":\"SUSPEITO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void arquivarRetorna200QuandoSucesso() throws Exception {
        mockMvc.perform(delete("/api/alertas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta arquivado com sucesso."));
        verify(alertaService).arquivarAlerta("1");
    }

    @Test
    void arquivarRetorna404QuandoNaoEncontrado() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Alerta não encontrado")).when(alertaService).arquivarAlerta("999");

        mockMvc.perform(delete("/api/alertas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mockMvc.perform(post("/api/alertas")
                        .contentType("application/json")
                        .content("{ isto não é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void parametroComTipoErradoRetorna400() throws Exception {
        mockMvc.perform(get("/api/alertas").param("lat", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Valor inválido para o parâmetro 'lat'."));
    }

    @Test
    void listarComStatusEmBrancoNaoFiltra() throws Exception {
        when(alertaService.buscarAlertasAtivos(null, null, null, null, null, null)).thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/alertas").param("status", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }
}
