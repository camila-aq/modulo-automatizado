package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.Reserva;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlOcupacionRepository
        extends JpaRepository<ControlOcupacion, Integer> {

    Optional<ControlOcupacion> findByReserva(
            Reserva reserva
    );
}