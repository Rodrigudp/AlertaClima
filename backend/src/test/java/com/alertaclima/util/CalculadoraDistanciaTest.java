package com.alertaclima.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CalculadoraDistanciaTest {

    @Test
    void distanciaEntreMesmoPontoEhZero() {
        assertThat(CalculadoraDistancia.distanciaEmKm(-23.42, -51.93, -23.42, -51.93)).isZero();
    }

    @Test
    void distanciaSaoPauloRioDeJaneiroEhAproximadamente360Km() {
        double km = CalculadoraDistancia.distanciaEmKm(-23.5505, -46.6333, -22.9068, -43.1729);
        assertThat(km).isCloseTo(360, within(10.0));
    }
}
