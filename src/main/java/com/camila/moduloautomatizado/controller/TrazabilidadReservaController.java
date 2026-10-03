package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.dto.ReservaTrazabilidadResumenResponse;
import com.camila.moduloautomatizado.dto.TrazabilidadReservaResponse;
import com.camila.moduloautomatizado.service.TrazabilidadReservaService;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trazabilidad")
public class TrazabilidadReservaController {

    private final TrazabilidadReservaService trazabilidadReservaService;


    public TrazabilidadReservaController(
            TrazabilidadReservaService trazabilidadReservaService) {

        this.trazabilidadReservaService =
                trazabilidadReservaService;
    }


    @GetMapping("/reservas")
    public List<ReservaTrazabilidadResumenResponse>
    buscarReservas(
            @RequestParam(required = false)
            String estudiante,

            @RequestParam(required = false)
            String codigoUniversitario,

            @RequestParam(required = false)
            String dni,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaHasta,

            @RequestParam(required = false)
            Integer idUbicacion,

            @RequestParam(required = false)
            Integer idAmbiente,

            @RequestParam(required = false)
            String estado) {

        return trazabilidadReservaService
                .buscarReservas(
                        estudiante,
                        codigoUniversitario,
                        dni,
                        fechaDesde,
                        fechaHasta,
                        idUbicacion,
                        idAmbiente,
                        estado
                );
    }


    @GetMapping("/reservas/{idReserva}")
    public TrazabilidadReservaResponse
    obtenerTrazabilidad(
            @PathVariable
            Integer idReserva) {

        return trazabilidadReservaService
                .obtenerTrazabilidad(
                        idReserva
                );
    }
}