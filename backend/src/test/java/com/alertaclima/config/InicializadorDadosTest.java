package com.alertaclima.config;

import com.alertaclima.model.Alerta;
import com.alertaclima.repository.AlertaRepository;
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
class InicializadorDadosTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private InicializadorDados inicializadorDados;

    @Test
    void runLimpaEPopula10AlertasComUsuariosEmbutidos() throws Exception {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        inicializadorDados.run();

        verify(alertaRepository).deleteAll();

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository, times(10)).save(captor.capture());

        List<Alerta> salvos = captor.getAllValues();
        assertThat(salvos).hasSize(10);
        assertThat(salvos).allSatisfy(a -> {
            assertThat(a.getCriado_por()).isNotNull();
            assertThat(a.getCriado_por().getPapel()).isEqualTo("CITIZEN");
        });
        assertThat(salvos).anySatisfy(a -> {
            assertThat(a.getValidado_por()).isNotNull();
            assertThat(a.getValidado_por().getPapel()).isEqualTo("ANALYST");
        });
    }
}
