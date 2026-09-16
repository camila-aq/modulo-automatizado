package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservaEstadoRepository extends JpaRepository<ReservaEstado, Integer> {

    Optional<ReservaEstado> findTopByReservaOrderByFechaHoraEstadoDesc(Reserva reserva);
}