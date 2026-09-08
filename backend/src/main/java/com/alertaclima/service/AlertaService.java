package com.alertaclima.service;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertaService {
    @Autowired
    private AlertaRepository alertaRepository;

    public Alerta criarAlerta(Alerta alerta) {
        if(alerta.getStatus() == null) alerta.setStatus("SUSPEITO");
        alerta.setCodigo(alertaRepository.count() + 1);
        LocalDateTime agora = LocalDateTime.now();
        alerta.setCriadoEm(agora);
        alerta.setAtualizadoEm(agora);
        return alertaRepository.save(alerta);
    }

    public List<Alerta> buscarAlertasAtivos(String tipoEvento, String status, String nivelPerigo, Double lat, Double lon, Double distancia) {
        List<Alerta> alertas = alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc();

        return alertas.stream().filter(a -> {
            boolean match = true;
            if(tipoEvento != null && !tipoEvento.isEmpty() && !tipoEvento.equals(a.getTipo_evento())) match = false;
            if(status != null && !status.isEmpty() && !status.equals(a.getStatus())) match = false;
            if(nivelPerigo != null && !nivelPerigo.isEmpty() && !nivelPerigo.equals(a.getNivel_perigo())) match = false;
            if(lat != null && lon != null && distancia != null && a.getLatitude() != null && a.getLongitude() != null) {
                double dist = calcularDistanciaEmKm(lat, lon, a.getLatitude(), a.getLongitude());
                if(dist > distancia) match = false;
            }
            return match;
        }).collect(Collectors.toList());
    }

    public List<Alerta> buscarAlertasArquivados() {
        return alertaRepository.findByArquivadoEmIsNotNullOrderByArquivadoEmDesc();
    }

    public Alerta buscarAlertaPorId(String id) {
        return alertaRepository.findById(id).orElse(null);
    }

    public Alerta atualizarAlerta(String id, Alerta dadosAtualizacao) {
        Alerta alerta = buscarAlertaPorId(id);
        if(alerta != null) {
            if(dadosAtualizacao.getNivel_perigo() != null) alerta.setNivel_perigo(dadosAtualizacao.getNivel_perigo());
            alerta.setAtualizadoEm(LocalDateTime.now());
            return alertaRepository.save(alerta);
        }
        return null;
    }

    public Alerta atualizarStatus(String id, String status, InformacoesUsuario validadoPor, String notas) {
        Alerta alerta = buscarAlertaPorId(id);
        if(alerta != null) {
            alerta.setStatus(status);
            alerta.setValidado_por(validadoPor);
            alerta.setNotas_validacao(notas);
            alerta.setAtualizadoEm(LocalDateTime.now());
            return alertaRepository.save(alerta);
        }
        return null;
    }

    public boolean arquivarAlerta(String id) {
        Alerta alerta = buscarAlertaPorId(id);
        if(alerta != null) {
            alerta.setArquivadoEm(LocalDateTime.now());
            alertaRepository.save(alerta);
            return true;
        }
        return false;
    }

    private double calcularDistanciaEmKm(double lat1, double lon1, double lat2, double lon2) {
      double R = 6371;
      double dLat = (lat2 - lat1) * (Math.PI / 180);
      double dLon = (lon2 - lon1) * (Math.PI / 180);
      double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    }
}
