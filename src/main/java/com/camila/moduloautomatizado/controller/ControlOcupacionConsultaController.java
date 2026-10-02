package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.dto.ControlOcupacionResponse;
import com.camila.moduloautomatizado.service.ControlOcupacionConsultaService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control-ocupacion")
public class ControlOcupacionConsultaController {

    private final ControlOcupacionConsultaService
            controlOcupacionConsultaService;

    public ControlOcupacionConsultaController(
            ControlOcupacionConsultaService controlOcupacionConsultaService) {

        this.controlOcupacionConsultaService = controlOcupacionConsultaService;
    }

    @GetMapping("/reservas/{idReserva}")
    public ControlOcupacionResponse obtenerControl(
            @PathVariable Integer idReserva) {

        return controlOcupacionConsultaService.obtenerControl(idReserva);
    }
}