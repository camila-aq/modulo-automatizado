package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record ReservaTrazabilidadResumenResponse(
        Integer idReserva,
        String codigoReserva,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        Integer idUbicacion,
        String ubicacion,
        String ambiente,
        String piso,
        String estadoActual
) {
}