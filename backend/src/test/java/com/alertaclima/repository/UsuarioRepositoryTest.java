package com.alertaclima.repository;

import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.RegiaoMonitorada;
import com.alertaclima.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest(properties = "de.flapdoodle.mongodb.embedded.version=6.0.6")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limpar() {
        usuarioRepository.deleteAll();
    }

    @Test
    void buscaPorEmailEVerificaExistencia() {
        usuarioRepository.save(novo("Maria", "maria@exemplo.com", PapelUsuario.CITIZEN));

        assertThat(usuarioRepository.findByEmail("maria@exemplo.com")).get()
                .extracting(Usuario::getNome).isEqualTo("Maria");
        assertThat(usuarioRepository.existsByEmail("maria@exemplo.com")).isTrue();
        assertThat(usuarioRepository.existsByEmail("ninguem@exemplo.com")).isFalse();
    }

    @Test
    void filtraPorPapelEOrdenaPorNome() {
        usuarioRepository.save(novo("Zeca", "zeca@exemplo.com", PapelUsuario.CITIZEN));
        usuarioRepository.save(novo("Ana", "ana@exemplo.com", PapelUsuario.CITIZEN));
        usuarioRepository.save(novo("Bruno", "bruno@defesacivil.gov.br", PapelUsuario.ANALYST));

        assertThat(usuarioRepository.findByPapelOrderByNomeAsc(PapelUsuario.CITIZEN))
                .extracting(Usuario::getNome).containsExactly("Ana", "Zeca");
        assertThat(usuarioRepository.findAllByOrderByNomeAsc())
                .extracting(Usuario::getNome).containsExactly("Ana", "Bruno", "Zeca");
    }

    @Test
    void indiceUnicoImpedeEmailDuplicado() {
        usuarioRepository.save(novo("Maria", "maria@exemplo.com", PapelUsuario.CITIZEN));

        assertThatThrownBy(() -> usuarioRepository.save(novo("Outra Maria", "maria@exemplo.com", PapelUsuario.CITIZEN)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void persisteRegioesMonitoradasComoSubdocumentos() {
        Usuario usuario = novo("Maria", "maria@exemplo.com", PapelUsuario.CITIZEN);
        usuario.getRegioesMonitoradas().add(new RegiaoMonitorada("Casa", -23.55, -46.63, 10.0));
        usuario.getRegioesMonitoradas().add(new RegiaoMonitorada("Trabalho", -23.60, -46.70, 5.0));
        String id = usuarioRepository.save(usuario).getId();

        Usuario relido = usuarioRepository.findById(id).orElseThrow();

        assertThat(relido.getRegioesMonitoradas()).extracting(RegiaoMonitorada::getNome)
                .containsExactly("Casa", "Trabalho");
        assertThat(relido.monitora(-23.551, -46.631)).isTrue();
    }

    private Usuario novo(String nome, String email, PapelUsuario papel) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPapel(papel);
        usuario.setRegioesMonitoradas(new java.util.ArrayList<>(List.of()));
        return usuario;
    }
}
