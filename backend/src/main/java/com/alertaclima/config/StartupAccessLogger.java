package com.alertaclima.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupAccessLogger {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @EventListener(ApplicationReadyEvent.class)
    void logAccessUrls() {
        String baseUrl = "http://localhost:" + serverPort;

        System.out.println();
        System.out.println("AlertaClima backend pronto.");
        if (activeProfiles.contains("seed")) {
            System.out.println("Dados de exemplo: collection alerts recriada com 10 alertas ficticios.");
        }
        System.out.println("Acessos:");
        System.out.println("  API (alertas):     " + baseUrl + "/api/alerts");
        System.out.println("  Documentacao:      " + baseUrl + "/docs");
        System.out.println("  OpenAPI (JSON):    " + baseUrl + "/v3/api-docs");
        System.out.println();
    }
}
