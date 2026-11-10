package com.alertaclima.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AlertaTest {

    @Test
    void gettersSettersEqualsHashCodeEToStringFuncionam() {
        LocalDateTime agora = LocalDateTime.now();
        InformacoesUsuario criador = new InformacoesUsuario("u1", "Cidadão Exemplo", "cidadao@exemplo.com", PapelUsuario.CITIZEN);

        Alerta a1 = new Alerta();
        a1.setId("1");
        a1.setTitulo("Árvore caída");
        a1.setDescricao("Bloqueando a via");
        a1.setTipoEvento("Árvore caída");
        a1.setNivelPerigo("Alto");
        a1.setStatus(StatusAlerta.SUSPEITO);
        a1.setLatitude(-23.5505);
        a1.setLongitude(-46.6333);
        a1.setEndereco("Centro, São Paulo - SP");
        a1.setUrlImagem("https://example.com/img.jpg");
        a1.setDataEvento(agora);
        a1.setCriadoPor(criador);
        a1.setValidadoPor(null);
        a1.setNotasValidacao("nota");
        a1.setCriadoEm(agora);
        a1.setAtualizadoEm(agora);
        a1.setArquivadoEm(null);

        Alerta a2 = new Alerta();
        a2.setId("1");
        a2.setTitulo("Árvore caída");
        a2.setDescricao("Bloqueando a via");
        a2.setTipoEvento("Árvore caída");
        a2.setNivelPerigo("Alto");
        a2.setStatus(StatusAlerta.SUSPEITO);
        a2.setLatitude(-23.5505);
        a2.setLongitude(-46.6333);
        a2.setEndereco("Centro, São Paulo - SP");
        a2.setUrlImagem("https://example.com/img.jpg");
        a2.setDataEvento(agora);
        a2.setCriadoPor(criador);
        a2.setValidadoPor(null);
        a2.setNotasValidacao("nota");
        a2.setCriadoEm(agora);
        a2.setAtualizadoEm(agora);
        a2.setArquivadoEm(null);

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        assertThat(a1.toString()).contains("Árvore caída");
        assertThat(a1.getCriadoPor().getNome()).isEqualTo("Cidadão Exemplo");
    }
}
