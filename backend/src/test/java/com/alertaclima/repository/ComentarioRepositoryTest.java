package com.alertaclima.repository;

import com.alertaclima.model.Comentario;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.PapelUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(properties = "de.flapdoodle.mongodb.embedded.version=6.0.6")
class ComentarioRepositoryTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 10, 8, 0);

    @Autowired
    private ComentarioRepository comentarioRepository;

    private final InformacoesUsuario maria = new InformacoesUsuario("u-maria", "Maria", "maria@exemplo.com", PapelUsuario.CITIZEN);
    private final InformacoesUsuario analista = new InformacoesUsuario("u-analista", "Analista", "analista@defesacivil.gov.br", PapelUsuario.ANALYST);

    @BeforeEach
    void limpar() {
        comentarioRepository.deleteAll();
    }

    @Test
    void listaComentariosDoAlertaEmOrdemCronologica() {
        Comentario segundo = salvar("a1", analista, BASE.plusMinutes(30), "Equipe acionada");
        Comentario primeiro = salvar("a1", maria, BASE, "A árvore continua lá");
        salvar("a2", maria, BASE.plusMinutes(10), "Outro alerta");

        assertThat(comentarioRepository.findByAlertaIdOrderByCriadoEmAsc("a1"))
                .extracting(Comentario::getId).containsExactly(primeiro.getId(), segundo.getId());
        assertThat(comentarioRepository.countByAlertaId("a1")).isEqualTo(2);
        assertThat(comentarioRepository.countByAlertaId("inexistente")).isZero();
    }

    @Test
    void buscarPorAutorUsaReferenciaUsuarioIdDoMaisNovoParaOMaisAntigo() {
        Comentario antigo = salvar("a1", maria, BASE, "Primeiro");
        Comentario recente = salvar("a2", maria, BASE.plusHours(1), "Segundo");
        salvar("a1", analista, BASE.plusHours(2), "Do analista");

        assertThat(comentarioRepository.buscarPorAutor("u-maria"))
                .extracting(Comentario::getId).containsExactly(recente.getId(), antigo.getId());
    }

    private Comentario salvar(String alertaId, InformacoesUsuario autor, LocalDateTime criadoEm, String texto) {
        Comentario comentario = new Comentario();
        comentario.setAlertaId(alertaId);
        comentario.setAutor(autor);
        comentario.setCriadoEm(criadoEm);
        comentario.setTexto(texto);
        return comentarioRepository.save(comentario);
    }
}
