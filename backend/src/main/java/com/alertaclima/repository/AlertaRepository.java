package com.alertaclima.repository;

import com.alertaclima.model.Alerta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends MongoRepository<Alerta, String> {

    List<Alerta> findByArquivadoEmIsNullOrderByCriadoEmDesc();

    List<Alerta> findByArquivadoEmIsNotNullOrderByArquivadoEmDesc();

    /** Alertas criados por um usuário (referência criado_por.usuarioId -> usuarios._id). */
    @Query(value = "{ 'criado_por.usuarioId': ?0 }", sort = "{ 'criadoEm': -1 }")
    List<Alerta> buscarPorCriador(String usuarioId);
}
