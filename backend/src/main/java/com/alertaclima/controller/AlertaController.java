package com.alertaclima.controller;

import com.alertaclima.dto.AtualizacaoStatusRequest;
import com.alertaclima.model.Alerta;
import com.alertaclima.model.StatusAlerta;
import com.alertaclima.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Operações de CRUD e validação de alertas climáticos")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @PostMapping
    @Operation(summary = "Criar alerta", description = "Registra um novo alerta enviado por um cidadão.")
    public ResponseEntity<Map<String, Object>> criar(@RequestBody Alerta alerta) {
        Alerta criado = alertaService.criarAlerta(alerta);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Alerta registrado com sucesso.", "alerta", criado));
    }

    @GetMapping
    @Operation(summary = "Listar alertas ativos", description = "Retorna alertas não arquivados, com filtros opcionais por tipo, status, perigo e raio em km.")
    public ResponseEntity<List<Alerta>> listarAtivos(
            @Parameter(description = "Tipo do evento, ex.: Alagamento") @RequestParam(required = false) String tipoEvento,
            @Parameter(description = "Status do alerta, ex.: SUSPEITO ou EM ANÁLISE") @RequestParam(required = false) String status,
            @Parameter(description = "Nível de perigo, ex.: Alto") @RequestParam(required = false) String nivelPerigo,
            @Parameter(description = "Latitude de referência para filtro por raio") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude de referência para filtro por raio") @RequestParam(required = false) Double lon,
            @Parameter(description = "Raio máximo em quilômetros") @RequestParam(required = false) Double distance
    ) {
        StatusAlerta filtroStatus = status == null || status.isBlank() ? null : StatusAlerta.deValor(status);
        return ResponseEntity.ok(alertaService.buscarAlertasAtivos(tipoEvento, filtroStatus, nivelPerigo, lat, lon, distance));
    }

    @GetMapping("/arquivados")
    @Operation(summary = "Listar alertas arquivados", description = "Retorna alertas com soft delete (arquivadoEm preenchido).")
    public ResponseEntity<List<Alerta>> listarArquivados() {
        return ResponseEntity.ok(alertaService.buscarAlertasArquivados());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar alerta por ID")
    public ResponseEntity<Alerta> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(alertaService.buscarAlertaPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar alerta", description = "Atualiza campos do alerta, como nível de perigo.")
    public ResponseEntity<Alerta> atualizar(@PathVariable String id, @RequestBody Alerta alerta) {
        return ResponseEntity.ok(alertaService.atualizarAlerta(id, alerta));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Permite que um analista valide ou altere o status do alerta.")
    public ResponseEntity<Alerta> atualizarStatus(@PathVariable String id, @RequestBody AtualizacaoStatusRequest body) {
        return ResponseEntity.ok(alertaService.atualizarStatus(id, body.status(), body.validadoPor(), body.notasValidacao()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Arquivar alerta", description = "Realiza soft delete preenchendo arquivadoEm.")
    public ResponseEntity<Map<String, String>> arquivar(@PathVariable String id) {
        alertaService.arquivarAlerta(id);
        return ResponseEntity.ok(Map.of("message", "Alerta arquivado com sucesso."));
    }
}
