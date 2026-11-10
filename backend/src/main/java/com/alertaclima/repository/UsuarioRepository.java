package com.alertaclima.repository;

import com.alertaclima.model.PapelUsuario;
import com.alertaclima.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByPapelOrderByNomeAsc(PapelUsuario papel);

    List<Usuario> findAllByOrderByNomeAsc();
}
