package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record OcuparReservaResponse(
        Integer idReserva,
        String codigoReserva,
        String estado,
        Integer cantidadIntegrantes,
        Integer cantidadValidacionesRegistradas,
        LocalDateTime fechaHoraOcupacion
) {
}