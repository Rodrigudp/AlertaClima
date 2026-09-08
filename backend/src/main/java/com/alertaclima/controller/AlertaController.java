package com.alertaclima.controller;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.service.AlertaService;
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
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Operações de CRUD e validação de alertas climáticos")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @PostMapping
    @Operation(summary = "Criar alerta", description = "Registra um novo alerta enviado por um cidadão.")
    public ResponseEntity<?> criar(@RequestBody Alerta alerta) {
        Alerta criado = alertaService.criarAlerta(alerta);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Alerta registrado com sucesso.", "alerta", criado));
    }

    @GetMapping
    @Operation(summary = "Listar alertas ativos", description = "Retorna alertas não arquivados, com filtros opcionais por tipo, status, perigo e raio em km.")
    public ResponseEntity<List<Alerta>> listarAtivos(
            @Parameter(description = "Tipo do evento, ex.: Alagamento") @RequestParam(required = false) String tipo_evento,
            @Parameter(description = "Status do alerta, ex.: SUSPEITO") @RequestParam(required = false) String status,
            @Parameter(description = "Nível de perigo, ex.: Alto") @RequestParam(required = false) String nivel_perigo,
            @Parameter(description = "Latitude de referência para filtro por raio") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude de referência para filtro por raio") @RequestParam(required = false) Double lon,
            @Parameter(description = "Raio máximo em quilômetros") @RequestParam(required = false) Double distance
    ) {
        return ResponseEntity.ok(alertaService.buscarAlertasAtivos(tipo_evento, status, nivel_perigo, lat, lon, distance));
    }

    @GetMapping("/arquivados")
    @Operation(summary = "Listar alertas arquivados", description = "Retorna alertas com soft delete (arquivadoEm preenchido).")
    public ResponseEntity<List<Alerta>> listarArquivados() {
        return ResponseEntity.ok(alertaService.buscarAlertasArquivados());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar alerta por ID")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        Alerta alerta = alertaService.buscarAlertaPorId(id);
        if(alerta == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(alerta);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar alerta", description = "Atualiza campos do alerta, como nível de perigo.")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Alerta alerta) {
        Alerta atualizado = alertaService.atualizarAlerta(id, alerta);
        if(atualizado == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(atualizado);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Permite que um analista valide ou altere o status do alerta.")
    public ResponseEntity<?> atualizarStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        InformacoesUsuario validadoPor = body.get("validado_por") != null
                ? new ObjectMapper().convertValue(body.get("validado_por"), InformacoesUsuario.class)
                : null;
        String notas = (String) body.get("notas_validacao");

        Alerta atualizado = alertaService.atualizarStatus(id, status, validadoPor, notas);
        if(atualizado == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Arquivar alerta", description = "Realiza soft delete preenchendo arquivadoEm.")
    public ResponseEntity<?> arquivar(@PathVariable String id) {
        boolean sucesso = alertaService.arquivarAlerta(id);
        if(!sucesso) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alerta não encontrado"));
        return ResponseEntity.ok(Map.of("message", "Alerta arquivado com sucesso."));
    }
}
