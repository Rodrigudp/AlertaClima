package com.alertaclima.service;

import com.alertaclima.exception.RecursoNaoEncontradoException;
import com.alertaclima.exception.RegraNegocioException;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.RegiaoMonitorada;
import com.alertaclima.model.Usuario;
import com.alertaclima.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario criar(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new RegraNegocioException("Nome é obrigatório.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RegraNegocioException("E-mail é obrigatório.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail.");
        }
        if (usuario.getPapel() == null) {
            usuario.setPapel(PapelUsuario.CITIZEN);
        }
        if (usuario.getRegioesMonitoradas() == null) {
            usuario.setRegioesMonitoradas(new ArrayList<>());
        }
        usuario.getRegioesMonitoradas().forEach(this::validarRegiao);

        LocalDateTime agora = LocalDateTime.now();
        usuario.setId(null);
        usuario.setCriadoEm(agora);
        usuario.setAtualizadoEm(agora);
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar(PapelUsuario papel) {
        if (papel == null) {
            return usuarioRepository.findAllByOrderByNomeAsc();
        }
        return usuarioRepository.findByPapelOrderByNomeAsc(papel);
    }

    public Usuario buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    public Usuario atualizar(String id, Usuario dados) {
        Usuario usuario = buscarPorId(id);
        if (dados.getNome() != null && !dados.getNome().isBlank()) {
            usuario.setNome(dados.getNome());
        }
        if (dados.getTelefone() != null) {
            usuario.setTelefone(dados.getTelefone());
        }
        if (dados.getPapel() != null) {
            usuario.setPapel(dados.getPapel());
        }
        if (dados.getEmail() != null && !dados.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(dados.getEmail())) {
                throw new RegraNegocioException("Já existe um usuário com este e-mail.");
            }
            usuario.setEmail(dados.getEmail());
        }
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public Usuario adicionarRegiao(String id, RegiaoMonitorada regiao) {
        Usuario usuario = buscarPorId(id);
        validarRegiao(regiao);
        boolean nomeRepetido = usuario.getRegioesMonitoradas().stream()
                .anyMatch(r -> r.getNome().equalsIgnoreCase(regiao.getNome()));
        if (nomeRepetido) {
            throw new RegraNegocioException("O usuário já monitora uma região com este nome.");
        }
        usuario.getRegioesMonitoradas().add(regiao);
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public Usuario removerRegiao(String id, String nomeRegiao) {
        Usuario usuario = buscarPorId(id);
        boolean removida = usuario.getRegioesMonitoradas()
                .removeIf(r -> r.getNome().equalsIgnoreCase(nomeRegiao));
        if (!removida) {
            throw new RecursoNaoEncontradoException("Região não encontrada");
        }
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public void remover(String id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    /**
     * Liga o resumo de usuário recebido (em um alerta ou comentário) ao documento da coleção "usuarios",
     * procurando pelo id ou pelo e-mail. Quando encontra, devolve o resumo atualizado com a referência;
     * quando não encontra, mantém os dados recebidos.
     */
    public InformacoesUsuario vincular(InformacoesUsuario informacoes) {
        if (informacoes == null) {
            return null;
        }
        Optional<Usuario> usuario = Optional.empty();
        if (informacoes.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(informacoes.getUsuarioId());
        }
        if (usuario.isEmpty() && informacoes.getEmail() != null) {
            usuario = usuarioRepository.findByEmail(informacoes.getEmail());
        }
        return usuario.map(Usuario::resumo).orElse(informacoes);
    }

    private void validarRegiao(RegiaoMonitorada regiao) {
        if (regiao == null || !regiao.ehValida()) {
            throw new RegraNegocioException("Região inválida: informe nome, latitude, longitude e raioKm maior que zero.");
        }
    }
}
