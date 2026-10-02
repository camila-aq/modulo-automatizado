package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ValidacionIngresoRepository
        extends JpaRepository<ValidacionIngreso, Integer> {

    boolean existsByReservaUsuario(
            ReservaUsuario reservaUsuario
    );

    @Query("""
        SELECT COUNT(DISTINCT v.reservaUsuario.usuario.idUsuario)
        FROM ValidacionIngreso v
        WHERE v.reservaUsuario.reserva = :reserva
          AND v.medioValidacion <>
              com.camila.moduloautomatizado.model.enums.MedioValidacion.OCUPACION_MANUAL
        """)
    long contarUsuariosValidadosPorReserva(
            @Param("reserva")
            Reserva reserva
    );

    @Query("""
        SELECT v
        FROM ValidacionIngreso v
        JOIN FETCH v.reservaUsuario ru
        JOIN FETCH ru.usuario u
        WHERE ru.reserva = :reserva
          AND v.medioValidacion <>
              com.camila.moduloautomatizado.model.enums.MedioValidacion.OCUPACION_MANUAL
        ORDER BY v.fechaHoraValidacion ASC
        """)
    List<ValidacionIngreso>
    findValidacionesIngresoPorReserva(
            @Param("reserva")
            Reserva reserva
    );

}