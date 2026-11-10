package com.alertaclima.service;

import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.exception.RegraNegocioException;
import com.alertaclima.model.Alerta;
import com.alertaclima.model.Comentario;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.Usuario;
import com.alertaclima.repository.ComentarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private AlertaService alertaService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ComentarioService comentarioService;

    private final InformacoesUsuario autor = new InformacoesUsuario("u1", "Maria", "maria@exemplo.com", "CITIZEN");

    private Comentario comentario(String texto, InformacoesUsuario autor) {
        Comentario c = new Comentario();
        c.setTexto(texto);
        c.setAutor(autor);
        return c;
    }

    @Test
    void adicionarVinculaAlertaEAutorEPersiste() {
        when(alertaService.buscarAlertaPorId("a1")).thenReturn(new Alerta());
        when(usuarioService.vincular(autor)).thenReturn(autor);
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));

        Comentario salvo = comentarioService.adicionar("a1", comentario("  Água subindo  ", autor));

        assertThat(salvo.getAlertaId()).isEqualTo("a1");
        assertThat(salvo.getTexto()).isEqualTo("Água subindo");
        assertThat(salvo.getAutor().getUsuarioId()).isEqualTo("u1");
        assertThat(salvo.getCriadoEm()).isNotNull();
    }

    @Test
    void adicionarFalhaQuandoAlertaNaoExiste() {
        when(alertaService.buscarAlertaPorId("x")).thenReturn(null);

        assertThatThrownBy(() -> comentarioService.adicionar("x", comentario("oi", autor)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void adicionarRejeitaTextoVazioOuSemAutor() {
        when(alertaService.buscarAlertaPorId("a1")).thenReturn(new Alerta());

        assertThatThrownBy(() -> comentarioService.adicionar("a1", comentario("   ", autor)))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("texto");
        assertThatThrownBy(() -> comentarioService.adicionar("a1", comentario(null, autor)))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> comentarioService.adicionar("a1", comentario("ok", null)))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("autor");
    }

    @Test
    void listarPorAlertaRetornaComentariosDoAlerta() {
        Comentario c = comentario("oi", autor);
        when(alertaService.buscarAlertaPorId("a1")).thenReturn(new Alerta());
        when(comentarioRepository.findByAlertaIdOrderByCriadoEmAsc("a1")).thenReturn(List.of(c));

        assertThat(comentarioService.listarPorAlerta("a1")).containsExactly(c);
    }

    @Test
    void listarPorAutorVerificaUsuarioEConsultaReferencia() {
        Comentario c = comentario("oi", autor);
        when(usuarioService.buscarPorId("u1")).thenReturn(new Usuario());
        when(comentarioRepository.buscarPorAutor("u1")).thenReturn(List.of(c));

        assertThat(comentarioService.listarPorAutor("u1")).containsExactly(c);
    }

    @Test
    void removerApagaOuLancaExcecao() {
        Comentario c = comentario("oi", autor);
        when(comentarioRepository.findById("c1")).thenReturn(Optional.of(c));
        when(comentarioRepository.findById("c404")).thenReturn(Optional.empty());

        comentarioService.remover("c1");
        verify(comentarioRepository).delete(c);

        assertThatThrownBy(() -> comentarioService.remover("c404"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
