package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OcupacionEstadoRepository
        extends JpaRepository<OcupacionEstado, Integer> {

    Optional<OcupacionEstado>
    findTopByControlOcupacionOrderByFechaHoraEstadoDesc(
            ControlOcupacion controlOcupacion
    );
}