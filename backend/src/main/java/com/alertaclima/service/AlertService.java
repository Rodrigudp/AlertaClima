package com.alertaclima.service;

import com.alertaclima.model.Alert;
import com.alertaclima.model.UserInfo;
import com.alertaclima.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {
    @Autowired
    private AlertRepository alertRepository;

    public Alert createAlert(Alert alert) {
        if(alert.getStatus() == null) alert.setStatus("SUSPEITO");
        alert.setCode(alertRepository.count() + 1);
        LocalDateTime now = LocalDateTime.now();
        alert.setCreatedAt(now);
        alert.setUpdatedAt(now);
        return alertRepository.save(alert);
    }

    public List<Alert> getActiveAlerts(String eventType, String status, String dangerLevel, Double lat, Double lon, Double distance) {
        List<Alert> alerts = alertRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();

        return alerts.stream().filter(a -> {
            boolean match = true;
            if(eventType != null && !eventType.isEmpty() && !eventType.equals(a.getEvent_type())) match = false;
            if(status != null && !status.isEmpty() && !status.equals(a.getStatus())) match = false;
            if(dangerLevel != null && !dangerLevel.isEmpty() && !dangerLevel.equals(a.getDanger_level())) match = false;
            if(lat != null && lon != null && distance != null && a.getLatitude() != null && a.getLongitude() != null) {
                double dist = getDistanceFromLatLonInKm(lat, lon, a.getLatitude(), a.getLongitude());
                if(dist > distance) match = false;
            }
            return match;
        }).collect(Collectors.toList());
    }

    public List<Alert> getArchivedAlerts() {
        return alertRepository.findByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    public Alert getAlertById(String id) {
        return alertRepository.findById(id).orElse(null);
    }

    public Alert updateAlert(String id, Alert updateData) {
        Alert alert = getAlertById(id);
        if(alert != null) {
            if(updateData.getDanger_level() != null) alert.setDanger_level(updateData.getDanger_level());
            alert.setUpdatedAt(LocalDateTime.now());
            return alertRepository.save(alert);
        }
        return null;
    }

    public Alert updateStatus(String id, String status, UserInfo validatedBy, String notes) {
        Alert alert = getAlertById(id);
        if(alert != null) {
            alert.setStatus(status);
            alert.setValidated_by(validatedBy);
            alert.setValidation_notes(notes);
            alert.setUpdatedAt(LocalDateTime.now());
            return alertRepository.save(alert);
        }
        return null;
    }

    public boolean archiveAlert(String id) {
        Alert alert = getAlertById(id);
        if(alert != null) {
            alert.setDeletedAt(LocalDateTime.now());
            alertRepository.save(alert);
            return true;
        }
        return false;
    }

    private double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
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
