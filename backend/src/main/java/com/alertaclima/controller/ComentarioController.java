package com.alertaclima.controller;

import com.alertaclima.model.Comentario;
import com.alertaclima.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Comentários", description = "Comentários de cidadãos e analistas sobre um alerta")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping("/alertas/{alertaId}/comentarios")
    @Operation(summary = "Listar comentários de um alerta")
    public ResponseEntity<List<Comentario>> listar(@PathVariable String alertaId) {
        return ResponseEntity.ok(comentarioService.listarPorAlerta(alertaId));
    }

    @PostMapping("/alertas/{alertaId}/comentarios")
    @Operation(summary = "Comentar em um alerta")
    public ResponseEntity<Comentario> adicionar(@PathVariable String alertaId, @RequestBody Comentario comentario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.adicionar(alertaId, comentario));
    }

    @DeleteMapping("/comentarios/{id}")
    @Operation(summary = "Remover comentário")
    public ResponseEntity<Map<String, String>> remover(@PathVariable String id) {
        comentarioService.remover(id);
        return ResponseEntity.ok(Map.of("message", "Comentário removido com sucesso."));
    }
}
