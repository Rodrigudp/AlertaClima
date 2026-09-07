package com.alertaclima.repository;

import com.alertaclima.model.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {

    List<Alert> findByDeletedAtIsNullOrderByCreatedAtDesc();

    List<Alert> findByDeletedAtIsNotNullOrderByDeletedAtDesc();
}
