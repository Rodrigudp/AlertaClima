package com.alertaclima.config;

import com.alertaclima.model.Alert;
import com.alertaclima.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void runClearsAndSeedsTenAlertsWithEmbeddedUsers() throws Exception {
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        dataInitializer.run();

        verify(alertRepository).deleteAll();

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(10)).save(captor.capture());

        List<Alert> saved = captor.getAllValues();
        assertThat(saved).hasSize(10);
        assertThat(saved).allSatisfy(a -> {
            assertThat(a.getCreated_by()).isNotNull();
            assertThat(a.getCreated_by().getRole()).isEqualTo("CITIZEN");
        });
        assertThat(saved).anySatisfy(a -> {
            assertThat(a.getValidated_by()).isNotNull();
            assertThat(a.getValidated_by().getRole()).isEqualTo("ANALYST");
        });
    }
}
