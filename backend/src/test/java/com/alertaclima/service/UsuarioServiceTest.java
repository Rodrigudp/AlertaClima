package com.alertaclima.service;

import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.exception.RegraNegocioException;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.RegiaoMonitorada;
import com.alertaclima.model.Usuario;
import com.alertaclima.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario maria;

    @BeforeEach
    void setUp() {
        maria = new Usuario();
        maria.setId("u1");
        maria.setNome("Maria");
        maria.setEmail("maria@exemplo.com");
        maria.setPapel("CITIZEN");
        maria.setRegioesMonitoradas(new ArrayList<>(List.of(new RegiaoMonitorada("Casa", -23.55, -46.63, 10.0))));
    }

    private Usuario novo(String nome, String email) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        return u;
    }

    @Test
    void criarDefinePapelPadraoEDatas() {
        when(usuarioRepository.existsByEmail("joao@exemplo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario criado = usuarioService.criar(novo("João", "joao@exemplo.com"));

        assertThat(criado.getPapel()).isEqualTo("CITIZEN");
        assertThat(criado.getCriadoEm()).isNotNull();
        assertThat(criado.getRegioesMonitoradas()).isEmpty();
    }

    @Test
    void criarComListaDeRegioesNulaGuardaListaVazia() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        Usuario entrada = novo("Ana", "ana@exemplo.com");
        entrada.setPapel("ANALYST");
        entrada.setRegioesMonitoradas(null);

        assertThat(usuarioService.criar(entrada).getRegioesMonitoradas()).isEmpty();
    }

    @Test
    void criarRejeitaNomeOuEmailVazios() {
        assertThatThrownBy(() -> usuarioService.criar(novo(" ", "a@a.com")))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("Nome");
        assertThatThrownBy(() -> usuarioService.criar(novo("Ana", null)))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("E-mail");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void criarRejeitaEmailDuplicado() {
        when(usuarioRepository.existsByEmail("maria@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(novo("Maria 2", "maria@exemplo.com")))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void criarRejeitaPapelInvalidoERegiaoInvalida() {
        Usuario papelErrado = novo("X", "x@x.com");
        papelErrado.setPapel("ADMIN");
        assertThatThrownBy(() -> usuarioService.criar(papelErrado)).isInstanceOf(RegraNegocioException.class);

        Usuario regiaoErrada = novo("Y", "y@y.com");
        regiaoErrada.getRegioesMonitoradas().add(new RegiaoMonitorada("Sem raio", -23.0, -46.0, 0.0));
        assertThatThrownBy(() -> usuarioService.criar(regiaoErrada)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void listarSemPapelRetornaTodosEComPapelFiltra() {
        when(usuarioRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(maria));
        when(usuarioRepository.findByPapelOrderByNomeAsc("ANALYST")).thenReturn(List.of());

        assertThat(usuarioService.listar(null)).containsExactly(maria);
        assertThat(usuarioService.listar("")).containsExactly(maria);
        assertThat(usuarioService.listar("ANALYST")).isEmpty();
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoExiste() {
        when(usuarioRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId("404"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizarAlteraCamposInformados() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));
        when(usuarioRepository.existsByEmail("nova@exemplo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario dados = novo("Maria Silva", "nova@exemplo.com");
        dados.setTelefone("44999990000");
        dados.setPapel("ANALYST");

        Usuario atualizado = usuarioService.atualizar("u1", dados);

        assertThat(atualizado.getNome()).isEqualTo("Maria Silva");
        assertThat(atualizado.getEmail()).isEqualTo("nova@exemplo.com");
        assertThat(atualizado.getTelefone()).isEqualTo("44999990000");
        assertThat(atualizado.getPapel()).isEqualTo("ANALYST");
    }

    @Test
    void atualizarIgnoraCamposVaziosEMantemEmailIgual() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario atualizado = usuarioService.atualizar("u1", novo("", "maria@exemplo.com"));

        assertThat(atualizado.getNome()).isEqualTo("Maria");
        verify(usuarioRepository, never()).existsByEmail(any());
    }

    @Test
    void atualizarRejeitaEmailDeOutroUsuario() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));
        when(usuarioRepository.existsByEmail("outro@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar("u1", novo(null, "outro@exemplo.com")))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void adicionarRegiaoIncluiSubdocumento() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario atualizado = usuarioService.adicionarRegiao("u1", new RegiaoMonitorada("Trabalho", -23.6, -46.7, 5.0));

        assertThat(atualizado.getRegioesMonitoradas()).extracting("nome").containsExactly("Casa", "Trabalho");
    }

    @Test
    void adicionarRegiaoRejeitaNomeRepetidoERegiaoInvalida() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));

        assertThatThrownBy(() -> usuarioService.adicionarRegiao("u1", new RegiaoMonitorada("casa", -23.0, -46.0, 5.0)))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> usuarioService.adicionarRegiao("u1", null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void removerRegiaoRetiraPeloNomeOuLancaExcecao() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> usuarioService.removerRegiao("u1", "Inexistente"))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        assertThat(usuarioService.removerRegiao("u1", "Casa").getRegioesMonitoradas()).isEmpty();
    }

    @Test
    void removerApagaUsuarioExistente() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));

        usuarioService.remover("u1");

        verify(usuarioRepository).delete(maria);
    }

    @Test
    void vincularRetornaNuloParaEntradaNula() {
        assertThat(usuarioService.vincular(null)).isNull();
    }

    @Test
    void vincularPorIdOuEmailDevolveResumoComReferencia() {
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(maria));
        when(usuarioRepository.findByEmail("maria@exemplo.com")).thenReturn(Optional.of(maria));

        InformacoesUsuario porId = usuarioService.vincular(new InformacoesUsuario("u1", null, null, null));
        InformacoesUsuario porEmail = usuarioService.vincular(new InformacoesUsuario(null, "M", "maria@exemplo.com", "CITIZEN"));

        assertThat(porId.getNome()).isEqualTo("Maria");
        assertThat(porEmail.getUsuarioId()).isEqualTo("u1");
    }

    @Test
    void vincularMantemDadosQuandoUsuarioNaoEstaCadastrado() {
        when(usuarioRepository.findByEmail("anonimo@exemplo.com")).thenReturn(Optional.empty());
        InformacoesUsuario anonimo = new InformacoesUsuario(null, "Anônimo", "anonimo@exemplo.com", "CITIZEN");

        assertThat(usuarioService.vincular(anonimo)).isSameAs(anonimo);
        assertThat(usuarioService.vincular(new InformacoesUsuario(null, "Sem email", null, null)).getNome()).isEqualTo("Sem email");
    }
}
