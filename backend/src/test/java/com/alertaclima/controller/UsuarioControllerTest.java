package com.alertaclima.controller;

import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.exception.RegraNegocioException;
import com.alertaclima.model.Alerta;
import com.alertaclima.model.Comentario;
import com.alertaclima.model.RegiaoMonitorada;
import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.Usuario;
import com.alertaclima.service.AlertaService;
import com.alertaclima.service.ComentarioService;
import com.alertaclima.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private AlertaService alertaService;

    @MockBean
    private ComentarioService comentarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("u1");
        usuario.setNome("Maria");
        usuario.setEmail("maria@exemplo.com");
        usuario.setPapel(PapelUsuario.CITIZEN);
        usuario.getRegioesMonitoradas().add(new RegiaoMonitorada("Casa", -23.55, -46.63, 10.0));
    }

    @Test
    void criarRetorna201ComRegioesAninhadas() throws Exception {
        when(usuarioService.criar(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios").contentType("application/json")
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.regioesMonitoradas[0].nome").value("Casa"));
    }

    @Test
    void criarRetorna400QuandoRegraDeNegocioFalha() throws Exception {
        when(usuarioService.criar(any(Usuario.class))).thenThrow(new RegraNegocioException("Já existe um usuário com este e-mail."));

        mockMvc.perform(post("/api/usuarios").contentType("application/json")
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
    }

    @Test
    void listarRepassaFiltroDePapel() throws Exception {
        when(usuarioService.listar(PapelUsuario.CITIZEN)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios").param("papel", "CITIZEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("maria@exemplo.com"));
    }

    @Test
    void buscarPorIdRetorna200Ou404() throws Exception {
        when(usuarioService.buscarPorId("u1")).thenReturn(usuario);
        when(usuarioService.buscarPorId("404")).thenThrow(new RecursoNaoEncontradoException("Usuário não encontrado"));

        mockMvc.perform(get("/api/usuarios/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria"));
        mockMvc.perform(get("/api/usuarios/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuário não encontrado"));
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(usuarioService.atualizar(eq("u1"), any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/usuarios/u1").contentType("application/json")
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk());
    }

    @Test
    void removerRetorna200() throws Exception {
        mockMvc.perform(delete("/api/usuarios/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário removido com sucesso."));
        verify(usuarioService).remover("u1");
    }

    @Test
    void adicionarERemoverRegiao() throws Exception {
        when(usuarioService.adicionarRegiao(eq("u1"), any(RegiaoMonitorada.class))).thenReturn(usuario);
        when(usuarioService.removerRegiao("u1", "Casa")).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/u1/regioes").contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RegiaoMonitorada("Trabalho", -23.6, -46.7, 5.0))))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/usuarios/u1/regioes/Casa"))
                .andExpect(status().isOk());
    }

    @Test
    void consultasRelacionadasAoUsuario() throws Exception {
        Alerta alerta = new Alerta();
        alerta.setId("a1");
        Comentario comentario = new Comentario();
        comentario.setId("c1");
        when(alertaService.buscarAlertasPorCriador("u1")).thenReturn(List.of(alerta));
        when(alertaService.buscarAlertasNasRegioesDoUsuario("u1")).thenReturn(List.of(alerta));
        when(comentarioService.listarPorAutor("u1")).thenReturn(List.of(comentario));

        mockMvc.perform(get("/api/usuarios/u1/alertas"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("a1"));
        mockMvc.perform(get("/api/usuarios/u1/alertas-monitorados"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("a1"));
        mockMvc.perform(get("/api/usuarios/u1/comentarios"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("c1"));
    }

    @Test
    void listarComPapelInvalidoRetorna400() throws Exception {
        mockMvc.perform(get("/api/usuarios").param("papel", "ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Valor inválido para o parâmetro 'papel'."));
        verifyNoInteractions(usuarioService);
    }

    @Test
    void criarComPapelInvalidoNoCorpoRetorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content("{\"nome\":\"X\",\"email\":\"x@x.com\",\"papel\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        verifyNoInteractions(usuarioService);
    }
}
