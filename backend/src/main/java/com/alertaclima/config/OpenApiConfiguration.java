package com.alertaclima.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI alertaClimaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AlertaClima API")
                        .description("API REST para registro colaborativo de alertas climáticos e de risco urbano.")
                        .version("v1"));
    }
}
