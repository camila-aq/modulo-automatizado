package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmbienteRepository extends JpaRepository<Ambiente, Integer> {

    Optional<Ambiente> findByCodigo(String codigo);

    List<Ambiente> findByUbicacionAndActivoTrueOrderByIdAmbienteAsc(
            Ubicacion ubicacion
    );
}