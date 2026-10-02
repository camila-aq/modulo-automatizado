package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.dto.CorreoElectronicoResponse;
import com.camila.moduloautomatizado.service.CorreoElectronicoConsultaService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/correos")
public class CorreoElectronicoConsultaController {

    private final CorreoElectronicoConsultaService correoElectronicoConsultaService;
    
    public CorreoElectronicoConsultaController(
            CorreoElectronicoConsultaService correoElectronicoConsultaService) {

        this.correoElectronicoConsultaService = correoElectronicoConsultaService;
    }

    @GetMapping("/reservas/{idReserva}")
    public List<CorreoElectronicoResponse> obtenerPorReserva(
            @PathVariable Integer idReserva) {

        return correoElectronicoConsultaService.obtenerPorReserva(idReserva);
    }
}