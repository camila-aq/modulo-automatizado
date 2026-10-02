package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ControlOcupacionResponse(
        Integer idReserva,
        String codigoReserva,
        LocalDateTime fechaHoraInicioControl,
        LocalDateTime fechaHoraLimiteTolerancia,
        String estadoActual,
        List<OcupacionEstadoResponse> historial
) {
}