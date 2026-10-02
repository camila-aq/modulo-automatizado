package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.dto.TrazabilidadReservaResponse;
import com.camila.moduloautomatizado.service.TrazabilidadReservaService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trazabilidad")
public class TrazabilidadReservaController {

    private final TrazabilidadReservaService trazabilidadReservaService;

    public TrazabilidadReservaController(
            TrazabilidadReservaService trazabilidadReservaService) {

        this.trazabilidadReservaService = trazabilidadReservaService;
    }


    @GetMapping("/reservas/{idReserva}")
    public TrazabilidadReservaResponse
    obtenerTrazabilidad(
            @PathVariable
            Integer idReserva) {

        return trazabilidadReservaService.obtenerTrazabilidad(idReserva);
    }
}