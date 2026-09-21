package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record PeriodoOcupacionResponse(
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin
) {
}