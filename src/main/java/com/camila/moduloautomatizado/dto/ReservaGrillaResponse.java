package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReservaGrillaResponse(
        Integer idReserva,
        Integer idAmbiente,
        String codigoReserva,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        LocalDateTime fechaHoraRegistro,
        String responsable,
        List<PeriodoOcupacionResponse> periodosOcupados
) {
}