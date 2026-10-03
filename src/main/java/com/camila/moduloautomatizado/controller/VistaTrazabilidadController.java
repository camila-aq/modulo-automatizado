package com.camila.moduloautomatizado.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaTrazabilidadController {

    @GetMapping("/trazabilidad/reservas")
    public String mostrarHistorialReservas() {

        return "trazabilidad/historial-reservas";
    }
}