package com.alertaclima.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AlertaTest {

    @Test
    void gettersSettersEqualsHashCodeEToStringFuncionam() {
        LocalDateTime agora = LocalDateTime.now();
        InformacoesUsuario criador = new InformacoesUsuario("u1", "Cidadão Exemplo", "cidadao@exemplo.com", "CITIZEN");

        Alerta a1 = new Alerta();
        a1.setId("1");
        a1.setTitulo("Árvore caída");
        a1.setDescricao("Bloqueando a via");
        a1.setTipo_evento("Árvore caída");
        a1.setNivel_perigo("Alto");
        a1.setStatus("SUSPEITO");
        a1.setLatitude(-23.5505);
        a1.setLongitude(-46.6333);
        a1.setEndereco("Centro, São Paulo - SP");
        a1.setUrl_imagem("https://example.com/img.jpg");
        a1.setData_evento(agora);
        a1.setCriado_por(criador);
        a1.setValidado_por(null);
        a1.setNotas_validacao("nota");
        a1.setCriadoEm(agora);
        a1.setAtualizadoEm(agora);
        a1.setArquivadoEm(null);

        Alerta a2 = new Alerta();
        a2.setId("1");
        a2.setTitulo("Árvore caída");
        a2.setDescricao("Bloqueando a via");
        a2.setTipo_evento("Árvore caída");
        a2.setNivel_perigo("Alto");
        a2.setStatus("SUSPEITO");
        a2.setLatitude(-23.5505);
        a2.setLongitude(-46.6333);
        a2.setEndereco("Centro, São Paulo - SP");
        a2.setUrl_imagem("https://example.com/img.jpg");
        a2.setData_evento(agora);
        a2.setCriado_por(criador);
        a2.setValidado_por(null);
        a2.setNotas_validacao("nota");
        a2.setCriadoEm(agora);
        a2.setAtualizadoEm(agora);
        a2.setArquivadoEm(null);

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        assertThat(a1.toString()).contains("Árvore caída");
        assertThat(a1.getCriado_por().getNome()).isEqualTo("Cidadão Exemplo");
    }
}
