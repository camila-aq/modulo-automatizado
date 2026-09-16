package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByDni(String dni);

    Optional<Usuario> findByCodigoUniversitario(String codigoUniversitario);
}