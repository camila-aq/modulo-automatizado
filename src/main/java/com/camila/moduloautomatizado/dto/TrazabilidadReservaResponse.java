package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TrazabilidadReservaResponse(

        Integer idReserva,

        String codigoReserva,

        String estadoActual,

        LocalDateTime fechaHoraLimiteTolerancia,

        List<EventoTrazabilidadResponse> eventos

) {
}