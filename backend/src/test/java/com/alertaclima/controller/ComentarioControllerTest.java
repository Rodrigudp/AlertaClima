package com.alertaclima.controller;

import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.model.Comentario;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.service.ComentarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComentarioController.class)
class ComentarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComentarioService comentarioService;

    private Comentario comentario() {
        Comentario c = new Comentario();
        c.setId("c1");
        c.setAlertaId("a1");
        c.setTexto("Água subindo");
        c.setAutor(new InformacoesUsuario("u1", "Maria", "maria@exemplo.com", "CITIZEN"));
        return c;
    }

    @Test
    void listarComentariosDoAlerta() throws Exception {
        when(comentarioService.listarPorAlerta("a1")).thenReturn(List.of(comentario()));

        mockMvc.perform(get("/api/alertas/a1/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].autor.usuarioId").value("u1"));
    }

    @Test
    void adicionarRetorna201() throws Exception {
        when(comentarioService.adicionar(eq("a1"), any(Comentario.class))).thenReturn(comentario());

        mockMvc.perform(post("/api/alertas/a1/comentarios").contentType("application/json")
                        .content(objectMapper.writeValueAsString(comentario())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertaId").value("a1"));
    }

    @Test
    void adicionarEmAlertaInexistenteRetorna404() throws Exception {
        when(comentarioService.adicionar(eq("x"), any(Comentario.class)))
                .thenThrow(new RecursoNaoEncontradoException("Alerta não encontrado"));

        mockMvc.perform(post("/api/alertas/x/comentarios").contentType("application/json")
                        .content(objectMapper.writeValueAsString(comentario())))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerRetorna200() throws Exception {
        mockMvc.perform(delete("/api/comentarios/c1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comentário removido com sucesso."));
        verify(comentarioService).remover("c1");
    }
}
