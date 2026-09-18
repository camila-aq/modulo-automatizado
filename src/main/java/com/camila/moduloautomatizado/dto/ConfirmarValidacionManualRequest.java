package com.camila.moduloautomatizado.dto;

import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

public record ConfirmarValidacionManualRequest(
        Integer idReservaUsuario,
        TipoIdentificador tipoIdentificador
) {
}