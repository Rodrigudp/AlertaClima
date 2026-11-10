package com.alertaclima.controller;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.Comentario;
import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.RegiaoMonitorada;
import com.alertaclima.model.Usuario;
import com.alertaclima.service.AlertaService;
import com.alertaclima.service.ComentarioService;
import com.alertaclima.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Cidadãos e analistas, com suas regiões monitoradas")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AlertaService alertaService;
    private final ComentarioService comentarioService;

    public UsuarioController(UsuarioService usuarioService, AlertaService alertaService, ComentarioService comentarioService) {
        this.usuarioService = usuarioService;
        this.alertaService = alertaService;
        this.comentarioService = comentarioService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar usuário")
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(usuario));
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Filtro opcional por papel (CITIZEN ou ANALYST).")
    public ResponseEntity<List<Usuario>> listar(
            @Parameter(description = "Papel do usuário") @RequestParam(required = false) PapelUsuario papel) {
        return ResponseEntity.ok(usuarioService.listar(papel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do usuário")
    public ResponseEntity<Usuario> atualizar(@PathVariable String id, @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.atualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário")
    public ResponseEntity<Map<String, String>> remover(@PathVariable String id) {
        usuarioService.remover(id);
        return ResponseEntity.ok(Map.of("message", "Usuário removido com sucesso."));
    }

    @PostMapping("/{id}/regioes")
    @Operation(summary = "Adicionar região monitorada", description = "Adiciona um subdocumento na lista regioesMonitoradas.")
    public ResponseEntity<Usuario> adicionarRegiao(@PathVariable String id, @RequestBody RegiaoMonitorada regiao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.adicionarRegiao(id, regiao));
    }

    @DeleteMapping("/{id}/regioes/{nome}")
    @Operation(summary = "Remover região monitorada")
    public ResponseEntity<Usuario> removerRegiao(@PathVariable String id, @PathVariable String nome) {
        return ResponseEntity.ok(usuarioService.removerRegiao(id, nome));
    }

    @GetMapping("/{id}/alertas")
    @Operation(summary = "Alertas criados pelo usuário")
    public ResponseEntity<List<Alerta>> alertasCriados(@PathVariable String id) {
        return ResponseEntity.ok(alertaService.buscarAlertasPorCriador(id));
    }

    @GetMapping("/{id}/alertas-monitorados")
    @Operation(summary = "Alertas nas regiões do usuário", description = "Alertas ativos dentro de alguma região monitorada pelo usuário.")
    public ResponseEntity<List<Alerta>> alertasMonitorados(@PathVariable String id) {
        return ResponseEntity.ok(alertaService.buscarAlertasNasRegioesDoUsuario(id));
    }

    @GetMapping("/{id}/comentarios")
    @Operation(summary = "Comentários feitos pelo usuário")
    public ResponseEntity<List<Comentario>> comentarios(@PathVariable String id) {
        return ResponseEntity.ok(comentarioService.listarPorAutor(id));
    }
}
