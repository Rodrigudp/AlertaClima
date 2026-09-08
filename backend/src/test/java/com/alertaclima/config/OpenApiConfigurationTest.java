package com.alertaclima.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigurationTest {

    @Test
    void alertaClimaOpenApiDefinesTitleAndVersion() {
        OpenApiConfiguration configuration = new OpenApiConfiguration();

        var openApi = configuration.alertaClimaOpenApi();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("AlertaClima API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
    }
}
