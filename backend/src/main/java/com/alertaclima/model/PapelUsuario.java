package com.alertaclima.model;

import java.util.Arrays;

public enum PapelUsuario {
    CITIZEN,
    ANALYST;

    public static boolean ehValido(String papel) {
        return papel != null && Arrays.stream(values()).anyMatch(p -> p.name().equals(papel));
    }
}
