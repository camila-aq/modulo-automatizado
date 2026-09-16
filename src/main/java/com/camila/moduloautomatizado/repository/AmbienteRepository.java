package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmbienteRepository extends JpaRepository<Ambiente, Integer> {

    Optional<Ambiente> findByCodigo(String codigo);
}