package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record ReservaValidacionManualResponse(
        Integer  idReservaUsuario,
        String codigoReserva,
        String codigoAmbiente,
        String nombreAmbiente,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String dni,
        String codigoUniversitario,
        String nombres,
        String apellidos
) {
}