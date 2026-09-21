package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record OcuparReservaRequest(
        LocalDateTime fechaHoraInicioPeriodo
) {
}