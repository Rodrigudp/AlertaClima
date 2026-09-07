package com.alertaclima.repository;

import com.alertaclima.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    @Query("SELECT a FROM Alert a WHERE a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<Alert> findAllActive();
    
    @Query("SELECT a FROM Alert a WHERE a.deletedAt IS NOT NULL ORDER BY a.deletedAt DESC")
    List<Alert> findAllArchived();
}
