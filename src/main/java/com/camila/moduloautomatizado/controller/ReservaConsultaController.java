package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.dto.ReservaGrillaResponse;
import com.camila.moduloautomatizado.service.ReservaConsultaService;
import com.camila.moduloautomatizado.dto.DetalleReservaResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaConsultaController {

    private final ReservaConsultaService reservaConsultaService;

    public ReservaConsultaController(
            ReservaConsultaService reservaConsultaService) {

        this.reservaConsultaService =
                reservaConsultaService;
    }

    @GetMapping
    public List<ReservaGrillaResponse> listarReservas(
            @RequestParam Integer idUbicacion,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return reservaConsultaService
                .obtenerReservas(
                        idUbicacion,
                        fecha
                );
    }

    @GetMapping("/{idReserva}")
    public DetalleReservaResponse obtenerDetalleReserva(
            @PathVariable Integer idReserva) {

        return reservaConsultaService
                .obtenerDetalleReserva(
                        idReserva
                );
    }
}