package com.alertaclima.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.assertj.core.api.Assertions.assertThatCode;

class CorsConfigTest {

    @Test
    void addCorsMappingsDoesNotThrow() {
        CorsConfig corsConfig = new CorsConfig();
        CorsRegistry registry = new CorsRegistry();

        assertThatCode(() -> corsConfig.addCorsMappings(registry)).doesNotThrowAnyException();
    }
}
