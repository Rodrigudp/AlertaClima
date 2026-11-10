package com.alertaclima.repository;

import com.alertaclima.model.Alerta;
import com.alertaclima.model.InformacoesUsuario;
import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.StatusAlerta;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes contra um MongoDB real (embarcado): validam as consultas derivadas, as @Query
 * escritas à mão e o formato dos documentos gravados.
 */
@DataMongoTest(properties = "de.flapdoodle.mongodb.embedded.version=6.0.6")
class AlertaRepositoryTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 10, 8, 0);

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private final InformacoesUsuario maria = new InformacoesUsuario("u-maria", "Maria", "maria@exemplo.com", PapelUsuario.CITIZEN);
    private final InformacoesUsuario joao = new InformacoesUsuario("u-joao", "João", "joao@exemplo.com", PapelUsuario.CITIZEN);

    @BeforeEach
    void limpar() {
        alertaRepository.deleteAll();
    }

    @Test
    void buscarPorCriadorRetornaSoAlertasAtivosDoUsuarioDoMaisNovoParaOMaisAntigo() {
        Alerta antigo = salvar("Antigo", maria, BASE, null);
        Alerta recente = salvar("Recente", maria, BASE.plusHours(2), null);
        salvar("Arquivado", maria, BASE.plusHours(1), BASE.plusHours(3));
        salvar("De outro usuário", joao, BASE.plusHours(4), null);

        List<Alerta> resultado = alertaRepository.buscarPorCriador("u-maria");

        assertThat(resultado).extracting(Alerta::getId).containsExactly(recente.getId(), antigo.getId());
    }

    @Test
    void separaAtivosEArquivadosComOrdenacaoCorreta() {
        Alerta ativo1 = salvar("Ativo 1", maria, BASE, null);
        Alerta ativo2 = salvar("Ativo 2", joao, BASE.plusHours(1), null);
        Alerta arquivadoAntes = salvar("Arquivado antes", maria, BASE, BASE.plusDays(1));
        Alerta arquivadoDepois = salvar("Arquivado depois", joao, BASE, BASE.plusDays(2));

        assertThat(alertaRepository.findByArquivadoEmIsNullOrderByCriadoEmDesc())
                .extracting(Alerta::getId).containsExactly(ativo2.getId(), ativo1.getId());
        assertThat(alertaRepository.findByArquivadoEmIsNotNullOrderByArquivadoEmDesc())
                .extracting(Alerta::getId).containsExactly(arquivadoDepois.getId(), arquivadoAntes.getId());
    }

    @Test
    void gravaHistoricoComoListaDeSubdocumentosEStatusPeloNomeDoEnum() {
        Alerta alerta = novo("Deslizamento", maria, BASE, null);
        alerta.setStatus(StatusAlerta.EM_ANALISE);
        alerta.registrarHistorico(StatusAlerta.SUSPEITO, maria, "Alerta registrado");
        alerta.registrarHistorico(StatusAlerta.EM_ANALISE, joao, "Equipe a caminho");
        Alerta salvo = alertaRepository.save(alerta);

        Document bruto = mongoTemplate.getCollection("alertas").find(new Document("_id", new org.bson.types.ObjectId(salvo.getId()))).first();
        assertThat(bruto).isNotNull();
        assertThat(bruto.getString("status")).isEqualTo("EM_ANALISE");
        assertThat(bruto.get("criadoPor", Document.class).getString("usuarioId")).isEqualTo("u-maria");
        List<Document> historico = bruto.getList("historico", Document.class);
        assertThat(historico).hasSize(2);
        assertThat(historico.get(1).getString("status")).isEqualTo("EM_ANALISE");
        assertThat(historico.get(1).get("responsavel", Document.class).getString("nome")).isEqualTo("João");

        Alerta relido = alertaRepository.findById(salvo.getId()).orElseThrow();
        assertThat(relido.getStatus()).isEqualTo(StatusAlerta.EM_ANALISE);
        assertThat(relido.getHistorico()).extracting(h -> h.getStatus())
                .containsExactly(StatusAlerta.SUSPEITO, StatusAlerta.EM_ANALISE);
    }

    private Alerta salvar(String titulo, InformacoesUsuario criador, LocalDateTime criadoEm, LocalDateTime arquivadoEm) {
        return alertaRepository.save(novo(titulo, criador, criadoEm, arquivadoEm));
    }

    private Alerta novo(String titulo, InformacoesUsuario criador, LocalDateTime criadoEm, LocalDateTime arquivadoEm) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(titulo);
        alerta.setStatus(StatusAlerta.SUSPEITO);
        alerta.setCriadoPor(criador);
        alerta.setCriadoEm(criadoEm);
        alerta.setArquivadoEm(arquivadoEm);
        return alerta;
    }
}
