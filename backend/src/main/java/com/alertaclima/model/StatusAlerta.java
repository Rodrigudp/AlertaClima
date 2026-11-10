package com.alertaclima.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Ciclo de vida de um alerta. Na API o status trafega pelo rótulo ("EM ANÁLISE");
 * no MongoDB é gravado pelo nome da constante ("EM_ANALISE").
 */
public enum StatusAlerta {
    SUSPEITO("SUSPEITO"),
    EM_ANALISE("EM ANÁLISE"),
    CONFIRMADO("CONFIRMADO"),
    RESOLVIDO("RESOLVIDO"),
    FALSO_ALARME("FALSO ALARME");

    private final String rotulo;

    StatusAlerta(String rotulo) {
        this.rotulo = rotulo;
    }

    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    /** Aceita o rótulo ("EM ANÁLISE") ou o nome da constante ("EM_ANALISE"). */
    @JsonCreator
    public static StatusAlerta deValor(String valor) {
        if (valor == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(s -> s.rotulo.equalsIgnoreCase(valor) || s.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Status inválido: " + valor + ". Use SUSPEITO, EM ANÁLISE, CONFIRMADO, RESOLVIDO ou FALSO ALARME."));
    }
}
