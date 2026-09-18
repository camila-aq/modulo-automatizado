package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record ReservaGrillaResponse(
        Integer idReserva,
        Integer idAmbiente,
        String codigoReserva,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        LocalDateTime fechaHoraRegistro,
        String estado,
        String responsable
) {
}