package com.camila.moduloautomatizado.dto;

public record AmbienteReservaResponse(
        Integer idAmbiente,
        String codigo,
        String nombre,
        String abreviatura,
        String piso,
        Integer cantidadMinima,
        Integer cantidadMaxima
) {
}