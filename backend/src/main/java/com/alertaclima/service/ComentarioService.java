package com.alertaclima.service;

import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.exception.RegraNegocioException;
import com.alertaclima.model.Comentario;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.repository.ComentarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final AlertaService alertaService;
    private final UsuarioService usuarioService;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             AlertaService alertaService,
                             UsuarioService usuarioService) {
        this.comentarioRepository = comentarioRepository;
        this.alertaService = alertaService;
        this.usuarioService = usuarioService;
    }

    public Comentario adicionar(String alertaId, Comentario comentario) {
        garantirQueAlertaExiste(alertaId);
        if (comentario.getTexto() == null || comentario.getTexto().isBlank()) {
            throw new RegraNegocioException("O texto do comentário é obrigatório.");
        }
        InformacoesUsuario autor = usuarioService.vincular(comentario.getAutor());
        if (autor == null) {
            throw new RegraNegocioException("O autor do comentário é obrigatório.");
        }
        comentario.setId(null);
        comentario.setAlertaId(alertaId);
        comentario.setAutor(autor);
        comentario.setTexto(comentario.getTexto().trim());
        comentario.setCriadoEm(LocalDateTime.now());
        return comentarioRepository.save(comentario);
    }

    public List<Comentario> listarPorAlerta(String alertaId) {
        garantirQueAlertaExiste(alertaId);
        return comentarioRepository.findByAlertaIdOrderByCriadoEmAsc(alertaId);
    }

    public List<Comentario> listarPorAutor(String usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return comentarioRepository.buscarPorAutor(usuarioId);
    }

    public void remover(String comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comentário não encontrado"));
        comentarioRepository.delete(comentario);
    }

    private void garantirQueAlertaExiste(String alertaId) {
        alertaService.buscarAlertaPorId(alertaId);
    }
}
