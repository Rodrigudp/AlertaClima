package com.alertaclima.controller;

import com.alertaclima.model.Alert;
import com.alertaclima.model.UserInfo;
import com.alertaclima.service.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alertas", description = "Operações de CRUD e validação de alertas climáticos")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    @PostMapping
    @Operation(summary = "Criar alerta", description = "Registra um novo alerta enviado por um cidadão.")
    public ResponseEntity<?> create(@RequestBody Alert alert) {
        Alert created = alertService.createAlert(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Alerta registrado com sucesso.", "alert", created));
    }
    
    @GetMapping
    @Operation(summary = "Listar alertas ativos", description = "Retorna alertas não arquivados, com filtros opcionais por tipo, status, perigo e raio em km.")
    public ResponseEntity<List<Alert>> getAll(
            @Parameter(description = "Tipo do evento, ex.: Alagamento") @RequestParam(required = false) String event_type,
            @Parameter(description = "Status do alerta, ex.: SUSPEITO") @RequestParam(required = false) String status,
            @Parameter(description = "Nível de perigo, ex.: Alto") @RequestParam(required = false) String danger_level,
            @Parameter(description = "Latitude de referência para filtro por raio") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude de referência para filtro por raio") @RequestParam(required = false) Double lon,
            @Parameter(description = "Raio máximo em quilômetros") @RequestParam(required = false) Double distance
    ) {
        return ResponseEntity.ok(alertService.getActiveAlerts(event_type, status, danger_level, lat, lon, distance));
    }
    
    @GetMapping("/archived")
    @Operation(summary = "Listar alertas arquivados", description = "Retorna alertas com soft delete (deletedAt preenchido).")
    public ResponseEntity<List<Alert>> getArchived() {
        return ResponseEntity.ok(alertService.getArchivedAlerts());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar alerta por ID")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Alert alert = alertService.getAlertById(id);
        if(alert == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(alert);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar alerta", description = "Atualiza campos do alerta, como nível de perigo.")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Alert alert) {
        Alert updated = alertService.updateAlert(id, alert);
        if(updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Permite que um analista valide ou altere o status do alerta.")
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
    @Operation(summary = "Arquivar alerta", description = "Realiza soft delete preenchendo deletedAt.")
    public ResponseEntity<?> archive(@PathVariable String id) {
        boolean success = alertService.archiveAlert(id);
        if(!success) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(Map.of("message", "Alerta arquivado com sucesso."));
    }
}
