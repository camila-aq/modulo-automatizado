package com.camila.moduloautomatizado.dto;

import com.camila.moduloautomatizado.model.enums.RolEnReserva;

public record IntegranteReservaResponse(
        Integer idReservaUsuario,
        String codigoUniversitario,
        String dni,
        String nombres,
        String apellidos,
        RolEnReserva rolEnReserva,
        boolean validado
) {
}