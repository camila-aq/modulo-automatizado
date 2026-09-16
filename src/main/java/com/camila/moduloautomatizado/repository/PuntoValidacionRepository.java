package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PuntoValidacionRepository extends JpaRepository<PuntoValidacion, Integer> {

    Optional<PuntoValidacion> findByCodigoPuntoAndActivoTrue(String codigoPunto);
}