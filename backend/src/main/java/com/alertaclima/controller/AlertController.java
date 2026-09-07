package com.alertaclima.controller;

import com.alertaclima.model.Alert;
import com.alertaclima.model.UserInfo;
import com.alertaclima.service.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Alert alert) {
        Alert created = alertService.createAlert(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Alerta registrado com sucesso.", "alert", created));
    }
    
    @GetMapping
    public ResponseEntity<List<Alert>> getAll(
            @RequestParam(required = false) String event_type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String danger_level,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double distance
    ) {
        return ResponseEntity.ok(alertService.getActiveAlerts(event_type, status, danger_level, lat, lon, distance));
    }
    
    @GetMapping("/archived")
    public ResponseEntity<List<Alert>> getArchived() {
        return ResponseEntity.ok(alertService.getArchivedAlerts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Alert alert = alertService.getAlertById(id);
        if(alert == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(alert);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Alert alert) {
        Alert updated = alertService.updateAlert(id, alert);
        if(updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        UserInfo validatedBy = body.get("validated_by") != null
                ? new ObjectMapper().convertValue(body.get("validated_by"), UserInfo.class)
                : null;
        String notes = (String) body.get("validation_notes");
        
        Alert updated = alertService.updateStatus(id, status, validatedBy, notes);
        if(updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> archive(@PathVariable String id) {
        boolean success = alertService.archiveAlert(id);
        if(!success) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(Map.of("message", "Alerta arquivado com sucesso."));
    }
}
