package com.alertaclima.service;

import com.alertaclima.model.Alert;
import com.alertaclima.model.UserInfo;
import com.alertaclima.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private Alert alagamentoSP;
    private Alert vendavalRJ;

    @BeforeEach
    void setUp() {
        alagamentoSP = new Alert();
        alagamentoSP.setId("1");
        alagamentoSP.setTitle("Alagamento severo");
        alagamentoSP.setEvent_type("Alagamento");
        alagamentoSP.setStatus("SUSPEITO");
        alagamentoSP.setDanger_level("Alto");
        alagamentoSP.setLatitude(-23.5505);
        alagamentoSP.setLongitude(-46.6333);

        vendavalRJ = new Alert();
        vendavalRJ.setId("2");
        vendavalRJ.setTitle("Vendaval forte");
        vendavalRJ.setEvent_type("Vendaval");
        vendavalRJ.setStatus("CONFIRMADO");
        vendavalRJ.setDanger_level("Moderado");
        vendavalRJ.setLatitude(-22.9068);
        vendavalRJ.setLongitude(-43.1729);
    }

    @Test
    void createAlertDefaultsStatusToSuspeitoAndPersists() {
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert input = new Alert();
        input.setTitle("Árvore caída");

        Alert created = alertService.createAlert(input);

        assertThat(created.getStatus()).isEqualTo("SUSPEITO");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
        verify(alertRepository).save(input);
    }

    @Test
    void createAlertKeepsExplicitStatus() {
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert input = new Alert();
        input.setStatus("CONFIRMADO");

        Alert created = alertService.createAlert(input);

        assertThat(created.getStatus()).isEqualTo("CONFIRMADO");
    }

    @Test
    void getActiveAlertsFiltersByEventTypeStatusAndDangerLevel() {
        when(alertRepository.findByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        List<Alert> result = alertService.getActiveAlerts("Vendaval", null, null, null, null, null);

        assertThat(result).containsExactly(vendavalRJ);
    }

    @Test
    void getActiveAlertsFiltersByStatusAndDangerLevel() {
        when(alertRepository.findByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        List<Alert> byStatus = alertService.getActiveAlerts(null, "CONFIRMADO", null, null, null, null);
        assertThat(byStatus).containsExactly(vendavalRJ);

        List<Alert> byDanger = alertService.getActiveAlerts(null, null, "Alto", null, null, null);
        assertThat(byDanger).containsExactly(alagamentoSP);
    }

    @Test
    void getActiveAlertsFiltersByRadiusAroundCoordinate() {
        when(alertRepository.findByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        // Perto de São Paulo, raio pequeno: só o alerta de SP deve entrar.
        List<Alert> nearSP = alertService.getActiveAlerts(null, null, null, -23.55, -46.63, 50.0);
        assertThat(nearSP).containsExactly(alagamentoSP);

        // Raio grande o suficiente para cobrir SP e RJ.
        List<Alert> wideRadius = alertService.getActiveAlerts(null, null, null, -23.55, -46.63, 1000.0);
        assertThat(wideRadius).containsExactlyInAnyOrder(alagamentoSP, vendavalRJ);
    }

    @Test
    void getActiveAlertsReturnsAllWhenNoFilters() {
        when(alertRepository.findByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        List<Alert> result = alertService.getActiveAlerts(null, null, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void getArchivedAlertsDelegatesToRepository() {
        when(alertRepository.findByDeletedAtIsNotNullOrderByDeletedAtDesc())
                .thenReturn(List.of(alagamentoSP));

        assertThat(alertService.getArchivedAlerts()).containsExactly(alagamentoSP);
    }

    @Test
    void getAlertByIdReturnsNullWhenMissing() {
        when(alertRepository.findById("999")).thenReturn(Optional.empty());

        assertThat(alertService.getAlertById("999")).isNull();
    }

    @Test
    void updateAlertChangesOnlyDangerLevel() {
        when(alertRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert patch = new Alert();
        patch.setDanger_level("Crítico");

        Alert updated = alertService.updateAlert("1", patch);

        assertThat(updated.getDanger_level()).isEqualTo("Crítico");
        assertThat(updated.getTitle()).isEqualTo("Alagamento severo");
    }

    @Test
    void updateAlertReturnsNullWhenAlertDoesNotExist() {
        when(alertRepository.findById("404")).thenReturn(Optional.empty());

        assertThat(alertService.updateAlert("404", new Alert())).isNull();
        verify(alertRepository, never()).save(any());
    }

    @Test
    void updateStatusSetsStatusValidatorAndNotes() {
        when(alertRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        UserInfo analyst = new UserInfo(null, "Analista", "analista@defesacivil.gov.br", "ANALYST");
        Alert updated = alertService.updateStatus("1", "CONFIRMADO", analyst, "Confirmado in loco");

        assertThat(updated.getStatus()).isEqualTo("CONFIRMADO");
        assertThat(updated.getValidated_by()).isEqualTo(analyst);
        assertThat(updated.getValidation_notes()).isEqualTo("Confirmado in loco");
    }

    @Test
    void archiveAlertSetsDeletedAtAndReturnsTrue() {
        when(alertRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));

        boolean result = alertService.archiveAlert("1");

        assertThat(result).isTrue();
        assertThat(alagamentoSP.getDeletedAt()).isNotNull();
        verify(alertRepository).save(alagamentoSP);
    }

    @Test
    void archiveAlertReturnsFalseWhenAlertDoesNotExist() {
        when(alertRepository.findById("404")).thenReturn(Optional.empty());

        assertThat(alertService.archiveAlert("404")).isFalse();
        verify(alertRepository, never()).save(any());
    }
}
