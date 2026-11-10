package com.alertaclima.repository;

import com.alertaclima.model.Comentario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends MongoRepository<Comentario, String> {

    List<Comentario> findByAlertaIdOrderByCriadoEmAsc(String alertaId);

    long countByAlertaId(String alertaId);

    @Query(value = "{ 'autor.usuarioId': ?0 }", sort = "{ 'criadoEm': -1 }")
    List<Comentario> buscarPorAutor(String usuarioId);
}
