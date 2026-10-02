package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record CorreoElectronicoResponse(
        Integer idCorreo,
        String tipoCorreo,
        String correoDestino,
        LocalDateTime fechaEnvio
) {
}