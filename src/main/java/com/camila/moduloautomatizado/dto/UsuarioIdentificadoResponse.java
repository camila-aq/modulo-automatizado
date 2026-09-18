package com.camila.moduloautomatizado.dto;

public record UsuarioIdentificadoResponse(
        String dni,
        String codigoUniversitario,
        String nombres,
        String apellidos
) {
}