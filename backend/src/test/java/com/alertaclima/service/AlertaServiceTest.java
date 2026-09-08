package com.alertaclima.service;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaService alertaService;

    private Alerta alagamentoSP;
    private Alerta vendavalRJ;

    @BeforeEach
    void setUp() {
        alagamentoSP = new Alerta();
        alagamentoSP.setId("1");
        alagamentoSP.setTitulo("Alagamento severo");
        alagamentoSP.setTipo_evento("Alagamento");
        alagamentoSP.setStatus("SUSPEITO");
        alagamentoSP.setNivel_perigo("Alto");
        alagamentoSP.setLatitude(-23.5505);
        alagamentoSP.setLongitude(-46.6333);

        vendavalRJ = new Alerta();
        vendavalRJ.setId("2");
        vendavalRJ.setTitulo("Vendaval forte");
        vendavalRJ.setTipo_evento("Vendaval");
        vendavalRJ.setStatus("CONFIRMADO");
        vendavalRJ.setNivel_perigo("Moderado");
        vendavalRJ.setLatitude(-22.9068);
        vendavalRJ.setLongitude(-43.1729);
    }

    @Test
    void criarAlertaDefineStatusPadraoSuspeitoEPersiste() {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta entrada = new Alerta();
        entrada.setTitulo("Árvore caída");

        Alerta criado = alertaService.criarAlerta(entrada);

        assertThat(criado.getStatus()).isEqualTo("SUSPEITO");
        assertThat(criado.getCriadoEm()).isNotNull();
        assertThat(criado.getAtualizadoEm()).isNotNull();
        verify(alertaRepository).save(entrada);
    }

    @Test
    void criarAlertaMantemStatusExplicito() {
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta entrada = new Alerta();
        entrada.setStatus("CONFIRMADO");

        Alerta criado = alertaService.criarAlerta(entrada);

        assertThat(criado.getStatus()).isEqualTo("CONFIRMADO");
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

        List<Alerta> porStatus = alertaService.buscarAlertasAtivos(null, "CONFIRMADO", null, null, null, null);
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
    void buscarAlertaPorIdRetornaNuloQuandoNaoEncontrado() {
        when(alertaRepository.findById("999")).thenReturn(Optional.empty());

        assertThat(alertaService.buscarAlertaPorId("999")).isNull();
    }

    @Test
    void atualizarAlertaAlteraApenasNivelPerigo() {
        when(alertaRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta patch = new Alerta();
        patch.setNivel_perigo("Crítico");

        Alerta atualizado = alertaService.atualizarAlerta("1", patch);

        assertThat(atualizado.getNivel_perigo()).isEqualTo("Crítico");
        assertThat(atualizado.getTitulo()).isEqualTo("Alagamento severo");
    }

    @Test
    void atualizarAlertaRetornaNuloQuandoAlertaNaoExiste() {
        when(alertaRepository.findById("404")).thenReturn(Optional.empty());

        assertThat(alertaService.atualizarAlerta("404", new Alerta())).isNull();
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void atualizarStatusDefineStatusValidadorENotas() {
        when(alertaRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        InformacoesUsuario analista = new InformacoesUsuario(null, "Analista", "analista@defesacivil.gov.br", "ANALYST");
        Alerta atualizado = alertaService.atualizarStatus("1", "CONFIRMADO", analista, "Confirmado in loco");

        assertThat(atualizado.getStatus()).isEqualTo("CONFIRMADO");
        assertThat(atualizado.getValidado_por()).isEqualTo(analista);
        assertThat(atualizado.getNotas_validacao()).isEqualTo("Confirmado in loco");
    }

    @Test
    void arquivarAlertaDefineArquivadoEmERetornaTrue() {
        when(alertaRepository.findById("1")).thenReturn(Optional.of(alagamentoSP));

        boolean resultado = alertaService.arquivarAlerta("1");

        assertThat(resultado).isTrue();
        assertThat(alagamentoSP.getArquivadoEm()).isNotNull();
        verify(alertaRepository).save(alagamentoSP);
    }

    @Test
    void arquivarAlertaRetornaFalseQuandoAlertaNaoExiste() {
        when(alertaRepository.findById("404")).thenReturn(Optional.empty());

        assertThat(alertaService.arquivarAlerta("404")).isFalse();
        verify(alertaRepository, never()).save(any());
    }
}
