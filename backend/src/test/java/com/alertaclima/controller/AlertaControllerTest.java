package com.alertaclima.controller;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
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
        alerta.setStatus("SUSPEITO");
        alerta.setNivel_perigo("Alto");
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
        when(alertaService.buscarAlertaPorId("999")).thenReturn(null);

        mockMvc.perform(get("/api/alertas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Alerta não encontrado"));
    }

    @Test
    void atualizarRetorna200QuandoEncontrado() throws Exception {
        when(alertaService.atualizarAlerta(eq("1"), any(Alerta.class))).thenReturn(alerta);

        mockMvc.perform(put("/api/alertas/1")
                        .contentType("application/json")
                        .content("{\"nivel_perigo\":\"Crítico\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarRetorna404QuandoNaoEncontrado() throws Exception {
        when(alertaService.atualizarAlerta(eq("999"), any(Alerta.class))).thenReturn(null);

        mockMvc.perform(put("/api/alertas/999")
                        .contentType("application/json")
                        .content("{\"nivel_perigo\":\"Crítico\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarStatusParseiaValidadoPorERetorna200() throws Exception {
        when(alertaService.atualizarStatus(eq("1"), eq("CONFIRMADO"), any(InformacoesUsuario.class), eq("ok")))
                .thenReturn(alerta);

        Map<String, Object> body = Map.of(
                "status", "CONFIRMADO",
                "notas_validacao", "ok",
                "validado_por", Map.of("nome", "Analista", "email", "a@a.com", "papel", "ANALYST")
        );

        mockMvc.perform(patch("/api/alertas/1/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(alertaService).atualizarStatus(eq("1"), eq("CONFIRMADO"), any(InformacoesUsuario.class), eq("ok"));
    }

    @Test
    void atualizarStatusSemValidadoPorPassaNulo() throws Exception {
        when(alertaService.atualizarStatus(eq("1"), eq("SUSPEITO"), isNull(), isNull()))
                .thenReturn(alerta);

        mockMvc.perform(patch("/api/alertas/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"SUSPEITO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarStatusRetorna404QuandoNaoEncontrado() throws Exception {
        when(alertaService.atualizarStatus(eq("999"), any(), any(), any())).thenReturn(null);

        mockMvc.perform(patch("/api/alertas/999/status")
                        .contentType("application/json")
                        .content("{\"status\":\"SUSPEITO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void arquivarRetorna200QuandoSucesso() throws Exception {
        when(alertaService.arquivarAlerta("1")).thenReturn(true);

        mockMvc.perform(delete("/api/alertas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta arquivado com sucesso."));
    }

    @Test
    void arquivarRetorna404QuandoNaoEncontrado() throws Exception {
        when(alertaService.arquivarAlerta("999")).thenReturn(false);

        mockMvc.perform(delete("/api/alertas/999"))
                .andExpect(status().isNotFound());
    }
}
