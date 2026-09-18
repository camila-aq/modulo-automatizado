package com.camila.moduloautomatizado.dto;

import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

import java.time.LocalDateTime;

public record ValidacionIngresoResponse(
        Integer idValidacion,
        Integer idReservaUsuario,
        MedioValidacion medioValidacion,
        TipoIdentificador tipoIdentificador,
        LocalDateTime fechaHoraValidacion,
        String codigoPunto
) {
}