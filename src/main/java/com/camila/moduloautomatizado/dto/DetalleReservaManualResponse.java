package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DetalleReservaManualResponse(
        Integer idReservaUsuarioBuscado,
        String codigoReserva,
        String codigoAmbiente,
        String nombreAmbiente,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        LocalDateTime fechaHoraRegistro,
        List<IntegranteReservaResponse> integrantes
) {
}