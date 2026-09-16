package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {

    Optional<Ubicacion> findByNombre(String nombre);
}