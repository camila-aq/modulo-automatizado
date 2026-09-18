package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository
        extends JpaRepository<Reserva, Integer> {

    Optional<Reserva> findByCodigoReserva(
            String codigoReserva
    );

    @Query("""
            SELECT r
            FROM Reserva r
            JOIN FETCH r.ambiente a
            WHERE a.ubicacion.idUbicacion = :idUbicacion
              AND r.fechaHoraInicio < :finDia
              AND r.fechaHoraFin > :inicioDia
            ORDER BY r.fechaHoraInicio ASC
            """)
    List<Reserva> buscarPorUbicacionYFecha(
            @Param("idUbicacion")
            Integer idUbicacion,

            @Param("inicioDia")
            LocalDateTime inicioDia,

            @Param("finDia")
            LocalDateTime finDia
    );
}