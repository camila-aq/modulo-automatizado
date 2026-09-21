package com.camila.moduloautomatizado.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaValidacionIngresoController {

    @GetMapping("/validaciones-ingreso/manual")
    public String mostrarValidacionManual() {

        return "validacion/manual";
    }


    @GetMapping("/validaciones-ingreso/escaneo")
    public String mostrarValidacionEscaneo() {

        return "validacion/escaneo";
    }
}