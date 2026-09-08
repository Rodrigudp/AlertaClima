package com.alertaclima.repository;

import com.alertaclima.model.Alerta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends MongoRepository<Alerta, String> {

    List<Alerta> findByArquivadoEmIsNullOrderByCriadoEmDesc();

    List<Alerta> findByArquivadoEmIsNotNullOrderByArquivadoEmDesc();
}
