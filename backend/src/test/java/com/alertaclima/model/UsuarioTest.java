package com.alertaclima.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    void resumoCarregaReferenciaEDadosBasicos() {
        Usuario u = new Usuario();
        u.setId("u1");
        u.setNome("Maria");
        u.setEmail("maria@exemplo.com");
        u.setPapel(PapelUsuario.CITIZEN);

        InformacoesUsuario resumo = u.resumo();

        assertThat(resumo).isEqualTo(new InformacoesUsuario("u1", "Maria", "maria@exemplo.com", PapelUsuario.CITIZEN));
    }

    @Test
    void monitoraVerificaSeCoordenadaEstaEmAlgumaRegiao() {
        Usuario u = new Usuario();
        u.setRegioesMonitoradas(new ArrayList<>(List.of(
                new RegiaoMonitorada("Maringá", -23.4205, -51.9333, 10.0),
                new RegiaoMonitorada("Curitiba", -25.4284, -49.2733, 10.0))));

        assertThat(u.monitora(-23.43, -51.94)).isTrue();   // Maringá
        assertThat(u.monitora(-25.43, -49.27)).isTrue();   // Curitiba
        assertThat(u.monitora(-22.90, -43.17)).isFalse();  // Rio de Janeiro

        u.setRegioesMonitoradas(null);
        assertThat(u.monitora(-23.43, -51.94)).isFalse();
    }

    @Test
    void regiaoMonitoradaValidaCampos() {
        assertThat(new RegiaoMonitorada("Casa", -23.0, -46.0, 5.0).ehValida()).isTrue();
        assertThat(new RegiaoMonitorada(null, -23.0, -46.0, 5.0).ehValida()).isFalse();
        assertThat(new RegiaoMonitorada(" ", -23.0, -46.0, 5.0).ehValida()).isFalse();
        assertThat(new RegiaoMonitorada("Casa", null, -46.0, 5.0).ehValida()).isFalse();
        assertThat(new RegiaoMonitorada("Casa", -23.0, null, 5.0).ehValida()).isFalse();
        assertThat(new RegiaoMonitorada("Casa", -23.0, -46.0, null).ehValida()).isFalse();
        assertThat(new RegiaoMonitorada("Casa", -23.0, -46.0, -1.0).ehValida()).isFalse();
    }

    @Test
    void regiaoNaoContemCoordenadaNulaNemQuandoInvalida() {
        RegiaoMonitorada regiao = new RegiaoMonitorada("Casa", -23.0, -46.0, 5.0);
        assertThat(regiao.contem(null, -46.0)).isFalse();
        assertThat(regiao.contem(-23.0, null)).isFalse();
        assertThat(new RegiaoMonitorada("Casa", -23.0, -46.0, 0.0).contem(-23.0, -46.0)).isFalse();
    }

    @Test
    void statusAlertaAceitaRotuloOuNomeDaConstante() {
        assertThat(StatusAlerta.deValor("EM ANÁLISE")).isEqualTo(StatusAlerta.EM_ANALISE);
        assertThat(StatusAlerta.deValor("EM_ANALISE")).isEqualTo(StatusAlerta.EM_ANALISE);
        assertThat(StatusAlerta.deValor("confirmado")).isEqualTo(StatusAlerta.CONFIRMADO);
        assertThat(StatusAlerta.deValor("FALSO ALARME")).isEqualTo(StatusAlerta.FALSO_ALARME);
        assertThat(StatusAlerta.deValor(null)).isNull();
        assertThat(StatusAlerta.EM_ANALISE.getRotulo()).isEqualTo("EM ANÁLISE");
    }

    @Test
    void statusAlertaRejeitaValorDesconhecido() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> StatusAlerta.deValor("ARQUIVADO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    void alertaRegistraHistoricoMesmoComListaNula() {
        Alerta alerta = new Alerta();
        alerta.setHistorico(null);

        alerta.registrarHistorico(StatusAlerta.SUSPEITO, null, "Alerta registrado");

        assertThat(alerta.getHistorico()).hasSize(1);
        RegistroHistorico registro = alerta.getHistorico().get(0);
        assertThat(registro.getStatus()).isEqualTo(StatusAlerta.SUSPEITO);
        assertThat(registro.getData()).isNotNull();
        assertThat(registro.getObservacao()).isEqualTo("Alerta registrado");
    }

    @Test
    void comentarioGuardaReferenciaAoAlerta() {
        Comentario c = new Comentario();
        c.setAlertaId("a1");
        c.setTexto("oi");
        assertThat(c.getAlertaId()).isEqualTo("a1");
        assertThat(c.toString()).contains("oi");
    }
}
