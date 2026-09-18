package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.service.ValidacionIngresoService;
import com.camila.moduloautomatizado.dto.ReservaValidacionManualResponse;
import com.camila.moduloautomatizado.dto.ConfirmarValidacionManualRequest;
import com.camila.moduloautomatizado.dto.ValidacionIngresoResponse;
import com.camila.moduloautomatizado.dto.UsuarioIdentificadoResponse;
import com.camila.moduloautomatizado.dto.ValidarIngresoEscaneoRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validaciones-ingreso")
public class ValidacionIngresoController {

    private final ValidacionIngresoService validacionIngresoService;

    public ValidacionIngresoController(ValidacionIngresoService validacionIngresoService) {
        this.validacionIngresoService =validacionIngresoService;
    }

    @GetMapping("/manual/identificar")
    public UsuarioIdentificadoResponse identificarUsuario(
            @RequestParam TipoIdentificador tipo,
            @RequestParam String valor) {

        Usuario usuario;

        if (tipo == TipoIdentificador.DNI) {
            usuario = validacionIngresoService
                    .identificarUsuarioPorDni(
                            valor
                    );
        } else if (tipo == TipoIdentificador.CODIGO_UNIVERSITARIO) {
            usuario =validacionIngresoService
                    .identificarUsuarioPorCodigoUniversitario(
                            valor
                    );
        } else {
            throw new IllegalArgumentException(
                    "El tipo de identificador no es válido."
            );
        }

        return new UsuarioIdentificadoResponse(
                usuario.getDni(),
                usuario.getCodigoUniversitario(),
                usuario.getNombres(),
                usuario.getApellidos()
        );
    }

    @GetMapping("/manual/reserva")
    public ReservaValidacionManualResponse buscarReservaManual(
            @RequestParam TipoIdentificador tipo,
            @RequestParam String valor) {

        Usuario usuario;

        if (tipo == TipoIdentificador.DNI) {

            usuario =
                    validacionIngresoService
                            .identificarUsuarioPorDni(
                                    valor
                            );

        } else if (
                tipo
                        == TipoIdentificador.CODIGO_UNIVERSITARIO
        ) {

            usuario =
                    validacionIngresoService
                            .identificarUsuarioPorCodigoUniversitario(
                                    valor
                            );

        } else {

            throw new IllegalArgumentException(
                    "El tipo de identificador no es válido."
            );
        }

        ReservaUsuario reservaUsuario =
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        );

        return new ReservaValidacionManualResponse(
                reservaUsuario.getIdReservaUsuario(),
                reservaUsuario
                        .getReserva()
                        .getCodigoReserva(),
                reservaUsuario
                        .getReserva()
                        .getAmbiente()
                        .getCodigo(),
                reservaUsuario
                        .getReserva()
                        .getAmbiente()
                        .getNombre(),
                reservaUsuario
                        .getReserva()
                        .getFechaHoraInicio(),
                reservaUsuario
                        .getReserva()
                        .getFechaHoraFin(),
                usuario.getDni(),
                usuario.getCodigoUniversitario(),
                usuario.getNombres(),
                usuario.getApellidos()
        );
    }

    @PostMapping("/manual/confirmar")
    public ValidacionIngresoResponse confirmarValidacionManual(
            @RequestBody ConfirmarValidacionManualRequest request) {

        if (request.idReservaUsuario() == null) {
            throw new IllegalArgumentException(
                    "La asociación del usuario con la reserva es obligatoria."
            );
        }

        if (request.tipoIdentificador() == null) {
            throw new IllegalArgumentException(
                    "El tipo de identificador es obligatorio."
            );
        }

        ValidacionIngreso validacion =
                validacionIngresoService
                        .confirmarValidacionManual(
                                request.idReservaUsuario(),
                                request.tipoIdentificador()
                        );

        return new ValidacionIngresoResponse(
                validacion.getIdValidacion(),
                validacion
                        .getReservaUsuario()
                        .getIdReservaUsuario(),
                validacion.getMedioValidacion(),
                validacion.getTipoIdentificador(),
                validacion.getFechaHoraValidacion(),
                validacion
                        .getPuntoValidacion()
                        .getCodigoPunto()
        );
    }

    @PostMapping("/escaneo")
    public ValidacionIngresoResponse validarIngresoPorEscaneo(
            @RequestBody ValidarIngresoEscaneoRequest request) {

        if (request.dni() == null || request.dni().isBlank()) {
            throw new IllegalArgumentException(
                    "El DNI es obligatorio."
            );
        }

        if (request.codigoPunto() == null
                || request.codigoPunto().isBlank()) {

            throw new IllegalArgumentException(
                    "El código del punto de validación es obligatorio."
            );
        }

        ValidacionIngreso validacion =
                validacionIngresoService
                        .validarIngresoPorEscaneo(
                                request.dni(),
                                request.codigoPunto()
                        );

        return new ValidacionIngresoResponse(
                validacion.getIdValidacion(),
                validacion
                        .getReservaUsuario()
                        .getIdReservaUsuario(),
                validacion.getMedioValidacion(),
                validacion.getTipoIdentificador(),
                validacion.getFechaHoraValidacion(),
                validacion
                        .getPuntoValidacion()
                        .getCodigoPunto()
        );
    }
}