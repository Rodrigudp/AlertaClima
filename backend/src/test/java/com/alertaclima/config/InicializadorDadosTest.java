package com.alertaclima.config;

import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.Alerta;
import com.alertaclima.model.Comentario;
import com.alertaclima.model.Usuario;
import com.alertaclima.repository.AlertaRepository;
import com.alertaclima.repository.ComentarioRepository;
import com.alertaclima.repository.UsuarioRepository;
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

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @InjectMocks
    private InicializadorDados inicializadorDados;

    @Test
    void runPopulaAsTresColecoesRelacionadas() throws Exception {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId("id-" + u.getEmail());
            return u;
        });
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));

        inicializadorDados.run();

        verify(comentarioRepository).deleteAll();
        verify(alertaRepository).deleteAll();
        verify(usuarioRepository).deleteAll();

        ArgumentCaptor<Usuario> usuarios = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(3)).save(usuarios.capture());
        assertThat(usuarios.getAllValues()).allSatisfy(u -> assertThat(u.getRegioesMonitoradas()).isNotEmpty());

        ArgumentCaptor<Alerta> alertas = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository, times(10)).save(alertas.capture());
        List<Alerta> salvos = alertas.getAllValues();
        assertThat(salvos).allSatisfy(a -> {
            assertThat(a.getCriadoPor().getUsuarioId()).startsWith("id-");
            assertThat(a.getCriadoPor().getPapel()).isEqualTo(PapelUsuario.CITIZEN);
            assertThat(a.getHistorico()).isNotEmpty();
        });
        assertThat(salvos).anySatisfy(a -> {
            assertThat(a.getValidadoPor().getPapel()).isEqualTo(PapelUsuario.ANALYST);
            assertThat(a.getHistorico()).hasSize(2);
        });

        verify(comentarioRepository, times(4)).save(any(Comentario.class));
    }
}
