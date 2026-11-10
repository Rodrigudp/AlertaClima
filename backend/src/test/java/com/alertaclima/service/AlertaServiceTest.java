package com.alertaclima.service;

import com.alertaclima.model.StatusAlerta;
import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.RegiaoMonitorada;
import com.alertaclima.model.Usuario;
import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.exception.RegraNegocioException;
import com.alertaclima.repository.AlertaRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AlertaService alertaService;

    private Alerta alagamentoSP;
    private Alerta vendavalRJ;

    @BeforeEach
    void setUp() {
        alagamentoSP = new Alerta();
        alagamentoSP.setId("1");
        alagamentoSP.setTitulo("Alagamento severo");
        alagamentoSP.setTipoEvento("Alagamento");
        alagamentoSP.setStatus(StatusAlerta.SUSPEITO);
        alagamentoSP.setNivelPerigo("Alto");
        alagamentoSP.setLatitude(-23.5505);
        alagamentoSP.setLongitude(-46.6333);

        vendavalRJ = new Alerta();
        vendavalRJ.setId("2");
        vendavalRJ.setTitulo("Vendaval forte");
        vendavalRJ.setTipoEvento("Vendaval");
        vendavalRJ.setStatus(StatusAlerta.CONFIRMADO);
        vendavalRJ.setNivelPerigo("Moderado");
        vendavalRJ.setLatitude(-22.9068);
        vendavalRJ.setLongitude(-43.1729);
    }

    @Test
    void criarAlertaDefineStatusPadraoSuspeitoEPersiste() {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta entrada = new Alerta();
        entrada.setTitulo("Árvore caída");

        Alerta criado = alertaService.criarAlerta(entrada);

        assertThat(criado.getStatus()).isEqualTo(StatusAlerta.SUSPEITO);
        assertThat(criado.getCriadoEm()).isNotNull();
        assertThat(criado.getAtualizadoEm()).isNotNull();
        verify(alertaRepository).save(entrada);
    }

    @Test
    void criarAlertaMantemStatusExplicito() {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta entrada = new Alerta();
        entrada.setStatus(StatusAlerta.CONFIRMADO);

        Alerta criado = alertaService.criarAlerta(entrada);

        assertThat(criado.getStatus()).isEqualTo(StatusAlerta.CONFIRMADO);
    }

    @Test
    void buscarAlertasAtivosFiltraPorTipoStatusENivelPerigo() {
        when(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        List<Alerta> resultado = alertaService.buscarAlertasAtivos("Vendaval", null, null, null, null, null);

        assertThat(resultado).containsExactly(vendavalRJ);
    }

    @Test
    void buscarAlertasAtivosFiltraPorStatusENivelPerigo() {
        when(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        List<Alerta> porStatus = alertaService.buscarAlertasAtivos(null, StatusAlerta.CONFIRMADO, null, null, null, null);
        assertThat(porStatus).containsExactly(vendavalRJ);

        List<Alerta> porPerigo = alertaService.buscarAlertasAtivos(null, null, "Alto", null, null, null);
        assertThat(porPerigo).containsExactly(alagamentoSP);
    }

    @Test
    void buscarAlertasAtivosFiltraPorRaioAoRedorDeCoordenada() {
        when(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        // Perto de São Paulo, raio pequeno: só o alerta de SP deve entrar.
        List<Alerta> pertoSP = alertaService.buscarAlertasAtivos(null, null, null, -23.55, -46.63, 50.0);
        assertThat(pertoSP).containsExactly(alagamentoSP);

        // Raio grande o suficiente para cobrir SP e RJ.
        List<Alerta> raioAmplo = alertaService.buscarAlertasAtivos(null, null, null, -23.55, -46.63, 1000.0);
        assertThat(raioAmplo).containsExactlyInAnyOrder(alagamentoSP, vendavalRJ);
    }

    @Test
    void buscarAlertasAtivosRetornaTodosSemFiltros() {
        when(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        List<Alerta> resultado = alertaService.buscarAlertasAtivos(null, null, null, null, null, null);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void buscarAlertasArquivadosDelegaParaRepositorio() {
        when(alertaRepository.findByArquivadoEmIsNotNullOrderByArquivadoEmDesc())
                .thenReturn(List.of(alagamentoSP));

        assertThat(alertaService.buscarAlertasArquivados()).containsExactly(alagamentoSP);
    }

    @Test
    void atualizarAlertaAlteraApenasNivelPerigo() {
        when(alertaRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta patch = new Alerta();
        patch.setNivelPerigo("Crítico");

        Alerta atualizado = alertaService.atualizarAlerta("1", patch);

        assertThat(atualizado.getNivelPerigo()).isEqualTo("Crítico");
        assertThat(atualizado.getTitulo()).isEqualTo("Alagamento severo");
    }

    @Test
    void atualizarAlertaLancaExcecaoQuandoAlertaNaoExiste() {
        when(alertaRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.atualizarAlerta("404", new Alerta()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void atualizarStatusDefineStatusValidadorENotas() {
        when(alertaRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        InformacoesUsuario analista = new InformacoesUsuario(null, "Analista", "analista@defesacivil.gov.br", PapelUsuario.ANALYST);
        when(usuarioService.vincular(analista)).thenReturn(analista);
        Alerta atualizado = alertaService.atualizarStatus("1", StatusAlerta.CONFIRMADO, analista, "Confirmado in loco");

        assertThat(atualizado.getStatus()).isEqualTo(StatusAlerta.CONFIRMADO);
        assertThat(atualizado.getValidadoPor()).isEqualTo(analista);
        assertThat(atualizado.getNotasValidacao()).isEqualTo("Confirmado in loco");
    }

    @Test
    void arquivarAlertaDefineArquivadoEm() {
        when(alertaRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));

        alertaService.arquivarAlerta("1");

        assertThat(alagamentoSP.getArquivadoEm()).isNotNull();
        verify(alertaRepository).save(alagamentoSP);
    }

    @Test
    void arquivarAlertaLancaExcecaoQuandoAlertaNaoExiste() {
        when(alertaRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.arquivarAlerta("404"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void criarAlertaVinculaCriadorAoUsuarioERegistraHistoricoInicial() {
        InformacoesUsuario enviado = new InformacoesUsuario(null, "Cidadão", "cidadao@exemplo.com", PapelUsuario.CITIZEN);
        InformacoesUsuario vinculado = new InformacoesUsuario("u1", "Cidadão", "cidadao@exemplo.com", PapelUsuario.CITIZEN);
        when(usuarioService.vincular(enviado)).thenReturn(vinculado);
        when(alertaRepository.count()).thenReturn(4L);
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta entrada = new Alerta();
        entrada.setCriadoPor(enviado);

        Alerta criado = alertaService.criarAlerta(entrada);

        assertThat(criado.getCodigo()).isEqualTo(5L);
        assertThat(criado.getCriadoPor().getUsuarioId()).isEqualTo("u1");
        assertThat(criado.getHistorico()).hasSize(1);
        assertThat(criado.getHistorico().get(0).getStatus()).isEqualTo(StatusAlerta.SUSPEITO);
        assertThat(criado.getHistorico().get(0).getResponsavel()).isEqualTo(vinculado);
    }

    @Test
    void atualizarStatusAcrescentaRegistroNoHistorico() {
        when(alertaRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.atualizarStatus("1", StatusAlerta.EM_ANALISE, null, "Equipe a caminho");
        Alerta atualizado = alertaService.atualizarStatus("1", StatusAlerta.CONFIRMADO, null, "Confirmado");

        assertThat(atualizado.getHistorico()).extracting("status").containsExactly(StatusAlerta.EM_ANALISE, StatusAlerta.CONFIRMADO);
    }

    @Test
    void atualizarStatusLancaExcecaoQuandoAlertaNaoExiste() {
        when(alertaRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.atualizarStatus("404", StatusAlerta.CONFIRMADO, null, null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void atualizarStatusExigeStatus() {
        assertThatThrownBy(() -> alertaService.atualizarStatus("1", null, null, "sem status"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O status é obrigatório.");
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void buscarAlertaPorIdLancaExcecaoQuandoNaoExiste() {
        when(alertaRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.buscarAlertaPorId("404"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Alerta não encontrado");
    }

    @Test
    void criarAlertaIgnoraIdEArquivamentoEnviadosPeloCliente() {
        Alerta entrada = new Alerta();
        entrada.setId("id-forjado");
        entrada.setArquivadoEm(java.time.LocalDateTime.now());
        when(alertaRepository.count()).thenReturn(0L);
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta criado = alertaService.criarAlerta(entrada);

        assertThat(criado.getId()).isNull();
        assertThat(criado.getArquivadoEm()).isNull();
        assertThat(criado.getStatus()).isEqualTo(StatusAlerta.SUSPEITO);
    }

    @Test
    void buscarAlertasPorCriadorConsultaPelaReferenciaDoUsuario() {
        when(usuarioService.buscarPorId("u1")).thenReturn(new Usuario());
        when(alertaRepository.buscarPorCriador("u1")).thenReturn(List.of(alagamentoSP));

        assertThat(alertaService.buscarAlertasPorCriador("u1")).containsExactly(alagamentoSP);
    }

    @Test
    void buscarAlertasPorCriadorFalhaQuandoUsuarioNaoExiste() {
        when(usuarioService.buscarPorId("x")).thenThrow(new RecursoNaoEncontradoException("Usuário não encontrado"));

        assertThatThrownBy(() -> alertaService.buscarAlertasPorCriador("x"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alertaRepository, never()).buscarPorCriador(any());
    }

    @Test
    void buscarAlertasNasRegioesDoUsuarioRetornaApenasAlertasDentroDasRegioes() {
        Usuario usuario = new Usuario();
        usuario.getRegioesMonitoradas().add(new RegiaoMonitorada("Casa", -23.55, -46.63, 30.0));
        when(usuarioService.buscarPorId("u1")).thenReturn(usuario);
        when(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        assertThat(alertaService.buscarAlertasNasRegioesDoUsuario("u1")).containsExactly(alagamentoSP);
    }

    @Test
    void buscarAlertasAtivosTrataFiltroVazioComoAusente() {
        when(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ));

        assertThat(alertaService.buscarAlertasAtivos("", null, "", null, null, null))
                .containsExactly(alagamentoSP, vendavalRJ);
    }

    @Test
    void filtroPorRaioSoSeAplicaComCoordenadaERaioCompletos() {
        Alerta semLocalizacao = new Alerta();
        semLocalizacao.setId("3");
        Alerta soLatitude = new Alerta();
        soLatitude.setId("4");
        soLatitude.setLatitude(-10.0);
        when(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .thenReturn(Arrays.asList(alagamentoSP, vendavalRJ, semLocalizacao, soLatitude));

        // Referência incompleta (sem longitude ou sem raio): o filtro de raio é ignorado.
        assertThat(alertaService.buscarAlertasAtivos(null, null, null, -23.55, null, 50.0)).hasSize(4);
        assertThat(alertaService.buscarAlertasAtivos(null, null, null, null, -46.63, 50.0)).hasSize(4);
        assertThat(alertaService.buscarAlertasAtivos(null, null, null, -23.55, -46.63, null)).hasSize(4);

        // Alertas sem coordenadas completas não são descartados pelo raio.
        assertThat(alertaService.buscarAlertasAtivos(null, null, null, -23.55, -46.63, 50.0))
                .containsExactly(alagamentoSP, semLocalizacao, soLatitude);
    }
}
