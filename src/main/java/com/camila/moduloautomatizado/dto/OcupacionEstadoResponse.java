package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record OcupacionEstadoResponse(
        String estado,
        LocalDateTime fechaHoraInicioPeriodo,
        LocalDateTime fechaHoraFinPeriodo,
        String motivo,
        LocalDateTime fechaHoraEstado
) {
}