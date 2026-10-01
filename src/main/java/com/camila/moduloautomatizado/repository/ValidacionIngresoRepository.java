package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ValidacionIngresoRepository
        extends JpaRepository<ValidacionIngreso, Integer> {

    boolean existsByReservaUsuario(
            ReservaUsuario reservaUsuario
    );

    @Query("""
            SELECT COUNT(DISTINCT v.reservaUsuario.usuario.idUsuario)
            FROM ValidacionIngreso v
            WHERE v.reservaUsuario.reserva = :reserva
            """)
    long contarUsuariosValidadosPorReserva(
            @Param("reserva")
            Reserva reserva
    );
}