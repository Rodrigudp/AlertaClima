package com.alertaclima.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AlertTest {

    @Test
    void gettersSettersEqualsHashCodeAndToStringWork() {
        LocalDateTime now = LocalDateTime.now();
        UserInfo creator = new UserInfo("u1", "Cidadão Exemplo", "cidadao@exemplo.com", "CITIZEN");

        Alert a1 = new Alert();
        a1.setId("1");
        a1.setTitle("Árvore caída");
        a1.setDescription("Bloqueando a via");
        a1.setEvent_type("Árvore caída");
        a1.setDanger_level("Alto");
        a1.setStatus("SUSPEITO");
        a1.setLatitude(-23.5505);
        a1.setLongitude(-46.6333);
        a1.setAddress("Centro, São Paulo - SP");
        a1.setImage_url("https://example.com/img.jpg");
        a1.setEvent_date(now);
        a1.setCreated_by(creator);
        a1.setValidated_by(null);
        a1.setValidation_notes("nota");
        a1.setCreatedAt(now);
        a1.setUpdatedAt(now);
        a1.setDeletedAt(null);

        Alert a2 = new Alert();
        a2.setId("1");
        a2.setTitle("Árvore caída");
        a2.setDescription("Bloqueando a via");
        a2.setEvent_type("Árvore caída");
        a2.setDanger_level("Alto");
        a2.setStatus("SUSPEITO");
        a2.setLatitude(-23.5505);
        a2.setLongitude(-46.6333);
        a2.setAddress("Centro, São Paulo - SP");
        a2.setImage_url("https://example.com/img.jpg");
        a2.setEvent_date(now);
        a2.setCreated_by(creator);
        a2.setValidated_by(null);
        a2.setValidation_notes("nota");
        a2.setCreatedAt(now);
        a2.setUpdatedAt(now);
        a2.setDeletedAt(null);

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        assertThat(a1.toString()).contains("Árvore caída");
        assertThat(a1.getCreated_by().getName()).isEqualTo("Cidadão Exemplo");
    }
}
