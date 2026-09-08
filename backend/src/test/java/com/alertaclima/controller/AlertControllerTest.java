package com.alertaclima.controller;

import com.alertaclima.model.Alert;
import com.alertaclima.model.UserInfo;
import com.alertaclima.service.AlertService;
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

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertService alertService;

    private Alert alert;

    @BeforeEach
    void setUp() {
        alert = new Alert();
        alert.setId("1");
        alert.setTitle("Alagamento severo");
        alert.setStatus("SUSPEITO");
        alert.setDanger_level("Alto");
    }

    @Test
    void createReturns201WithCreatedAlert() throws Exception {
        when(alertService.createAlert(any(Alert.class))).thenReturn(alert);

        mockMvc.perform(post("/api/alerts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(alert)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Alerta registrado com sucesso."))
                .andExpect(jsonPath("$.alert.id").value("1"));
    }

    @Test
    void getAllReturnsActiveAlerts() throws Exception {
        when(alertService.getActiveAlerts(null, null, null, null, null, null))
                .thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void getArchivedReturnsArchivedAlerts() throws Exception {
        when(alertService.getArchivedAlerts()).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts/archived"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void getByIdReturns200WhenFound() throws Exception {
        when(alertService.getAlertById("1")).thenReturn(alert);

        mockMvc.perform(get("/api/alerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alagamento severo"));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        when(alertService.getAlertById("999")).thenReturn(null);

        mockMvc.perform(get("/api/alerts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Alerta não encontrado"));
    }

    @Test
    void updateReturns200WhenFound() throws Exception {
        when(alertService.updateAlert(eq("1"), any(Alert.class))).thenReturn(alert);

        mockMvc.perform(put("/api/alerts/1")
                        .contentType("application/json")
                        .content("{\"danger_level\":\"Crítico\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateReturns404WhenMissing() throws Exception {
        when(alertService.updateAlert(eq("999"), any(Alert.class))).thenReturn(null);

        mockMvc.perform(put("/api/alerts/999")
                        .contentType("application/json")
                        .content("{\"danger_level\":\"Crítico\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusParsesValidatedByAndReturns200() throws Exception {
        when(alertService.updateStatus(eq("1"), eq("CONFIRMADO"), any(UserInfo.class), eq("ok")))
                .thenReturn(alert);

        Map<String, Object> body = Map.of(
                "status", "CONFIRMADO",
                "validation_notes", "ok",
                "validated_by", Map.of("name", "Analista", "email", "a@a.com", "role", "ANALYST")
        );

        mockMvc.perform(patch("/api/alerts/1/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(alertService).updateStatus(eq("1"), eq("CONFIRMADO"), any(UserInfo.class), eq("ok"));
    }

    @Test
    void updateStatusWithoutValidatedByPassesNull() throws Exception {
        when(alertService.updateStatus(eq("1"), eq("SUSPEITO"), isNull(), isNull()))
                .thenReturn(alert);

        mockMvc.perform(patch("/api/alerts/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"SUSPEITO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatusReturns404WhenMissing() throws Exception {
        when(alertService.updateStatus(eq("999"), any(), any(), any())).thenReturn(null);

        mockMvc.perform(patch("/api/alerts/999/status")
                        .contentType("application/json")
                        .content("{\"status\":\"SUSPEITO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void archiveReturns200WhenSuccessful() throws Exception {
        when(alertService.archiveAlert("1")).thenReturn(true);

        mockMvc.perform(delete("/api/alerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta arquivado com sucesso."));
    }

    @Test
    void archiveReturns404WhenMissing() throws Exception {
        when(alertService.archiveAlert("999")).thenReturn(false);

        mockMvc.perform(delete("/api/alerts/999"))
                .andExpect(status().isNotFound());
    }
}
