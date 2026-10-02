package com.camila.moduloautomatizado.dto;

import java.time.LocalDateTime;

public record EventoTrazabilidadResponse(

        String tipoEvento,

        LocalDateTime fechaHora,

        String detalle

) {
}