package com.alertaclima.model;

import com.alertaclima.util.CalculadoraDistancia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Subdocumento de Usuario: área (centro + raio) que o usuário quer acompanhar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegiaoMonitorada {
    private String nome;
    private Double latitude;
    private Double longitude;
    private Double raioKm;

    public boolean ehValida() {
        return nome != null && !nome.isBlank()
                && latitude != null && longitude != null
                && raioKm != null && raioKm > 0;
    }

    public boolean contem(Double lat, Double lon) {
        if (lat == null || lon == null || !ehValida()) {
            return false;
        }
        return CalculadoraDistancia.distanciaEmKm(latitude, longitude, lat, lon) <= raioKm;
    }
}
