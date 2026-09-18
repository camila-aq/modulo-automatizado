package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.service.ValidacionIngresoService;
import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;


import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class ValidacionIngresoControllerTest {

    private MockMvc mockMvc;

    private ValidacionIngresoService validacionIngresoService;

    @BeforeEach
    void configurar() {

        validacionIngresoService =
                mock(ValidacionIngresoService.class);

        ValidacionIngresoController controller =
                new ValidacionIngresoController(
                        validacionIngresoService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();
    }

    @Test
    @DisplayName("R3 Controller - Debe identificar un usuario por DNI")
    void debeIdentificarUsuarioPorDni() throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 1 R3] Identificando usuario por DNI..."
        );

        Usuario usuario = new Usuario();

        usuario.setDni(
                "10000001"
        );

        usuario.setCodigoUniversitario(
                "EST0000001"
        );

        usuario.setNombres(
                "Ana"
        );

        usuario.setApellidos(
                "Torres"
        );

        when(
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000001"
                        )
        ).thenReturn(
                usuario
        );

        mockMvc.perform(
                        get(
                                "/api/validaciones-ingreso/manual/identificar"
                        )
                                .param(
                                        "tipo",
                                        "DNI"
                                )
                                .param(
                                        "valor",
                                        "10000001"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.dni")
                                .value("10000001")
                )
                .andExpect(
                        jsonPath("$.codigoUniversitario")
                                .value("EST0000001")
                )
                .andExpect(
                        jsonPath("$.nombres")
                                .value("Ana")
                )
                .andExpect(
                        jsonPath("$.apellidos")
                                .value("Torres")
                );

        System.out.println(
                "[PRUEBA CONTROLLER 1 R3] OK - Respuesta HTTP correcta."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe devolver un mensaje cuando el DNI no existe")
    void debeDevolverErrorCuandoDniNoExiste() throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 2 R3] Probando DNI inexistente..."
        );

        when(
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "99999999"
                        )
        ).thenThrow(
                new IllegalArgumentException(
                        "El DNI no corresponde a un usuario registrado."
                )
        );

        mockMvc.perform(
                        get(
                                "/api/validaciones-ingreso/manual/identificar"
                        )
                                .param(
                                        "tipo",
                                        "DNI"
                                )
                                .param(
                                        "valor",
                                        "99999999"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "El DNI no corresponde a un usuario registrado."
                                )
                );

        System.out.println(
                "[PRUEBA CONTROLLER 2 R3] OK - Error HTTP controlado correctamente."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe identificar un usuario por código universitario")
    void debeIdentificarUsuarioPorCodigoUniversitario() throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 3 R3] Identificando usuario por código universitario..."
        );

        Usuario usuario = new Usuario();

        usuario.setDni(
                "10000001"
        );

        usuario.setCodigoUniversitario(
                "EST0000001"
        );

        usuario.setNombres(
                "Ana"
        );

        usuario.setApellidos(
                "Torres"
        );

        when(
                validacionIngresoService
                        .identificarUsuarioPorCodigoUniversitario(
                                "EST0000001"
                        )
        ).thenReturn(
                usuario
        );

        mockMvc.perform(
                        get(
                                "/api/validaciones-ingreso/manual/identificar"
                        )
                                .param(
                                        "tipo",
                                        "CODIGO_UNIVERSITARIO"
                                )
                                .param(
                                        "valor",
                                        "EST0000001"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.dni")
                                .value("10000001")
                )
                .andExpect(
                        jsonPath("$.codigoUniversitario")
                                .value("EST0000001")
                )
                .andExpect(
                        jsonPath("$.nombres")
                                .value("Ana")
                )
                .andExpect(
                        jsonPath("$.apellidos")
                                .value("Torres")
                );

        System.out.println(
                "[PRUEBA CONTROLLER 3 R3] OK - Respuesta HTTP correcta."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe devolver un mensaje cuando el código universitario no existe")
    void debeDevolverErrorCuandoCodigoUniversitarioNoExiste()
            throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 4 R3] Probando código universitario inexistente..."
        );

        when(
                validacionIngresoService
                        .identificarUsuarioPorCodigoUniversitario(
                                "EST9999999"
                        )
        ).thenThrow(
                new IllegalArgumentException(
                        "El código universitario no corresponde a un usuario registrado."
                )
        );

        mockMvc.perform(
                        get(
                                "/api/validaciones-ingreso/manual/identificar"
                        )
                                .param(
                                        "tipo",
                                        "CODIGO_UNIVERSITARIO"
                                )
                                .param(
                                        "valor",
                                        "EST9999999"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "El código universitario no corresponde a un usuario registrado."
                                )
                );

        System.out.println(
                "[PRUEBA CONTROLLER 4 R3] OK - Error HTTP controlado correctamente."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe localizar la reserva vigente del usuario")
    void debeLocalizarReservaVigenteDelUsuario() throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 5 R3] Buscando reserva vigente..."
        );

        Usuario usuario = new Usuario();

        usuario.setDni(
                "10000001"
        );

        usuario.setCodigoUniversitario(
                "EST0000001"
        );

        usuario.setNombres(
                "Ana"
        );

        usuario.setApellidos(
                "Torres"
        );

        Ambiente ambiente = new Ambiente();

        ambiente.setCodigo(
                "AMB-SIM-001"
        );

        ambiente.setNombre(
                "Sala de Estudio 101"
        );

        Reserva reserva = new Reserva();

        reserva.setCodigoReserva(
                "RES-SIM-001"
        );

        reserva.setAmbiente(
                ambiente
        );

        reserva.setFechaHoraInicio(
                LocalDateTime.of(
                        2026,
                        9,
                        17,
                        19,
                        0
                )
        );

        reserva.setFechaHoraFin(
                LocalDateTime.of(
                        2026,
                        9,
                        17,
                        21,
                        0
                )
        );

        ReservaUsuario reservaUsuario =
                new ReservaUsuario();

        reservaUsuario.setIdReservaUsuario(
                1
        );

        reservaUsuario.setUsuario(
                usuario
        );

        reservaUsuario.setReserva(
                reserva
        );

        when(
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000001"
                        )
        ).thenReturn(
                usuario
        );

        when(
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        )
        ).thenReturn(
                reservaUsuario
        );

        mockMvc.perform(
                        get(
                                "/api/validaciones-ingreso/manual/reserva"
                        )
                                .param(
                                        "tipo",
                                        "DNI"
                                )
                                .param(
                                        "valor",
                                        "10000001"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.idReservaUsuario")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.codigoReserva")
                                .value("RES-SIM-001")
                )
                .andExpect(
                        jsonPath("$.codigoAmbiente")
                                .value("AMB-SIM-001")
                )
                .andExpect(
                        jsonPath("$.nombreAmbiente")
                                .value("Sala de Estudio 101")
                )
                .andExpect(
                        jsonPath("$.dni")
                                .value("10000001")
                )
                .andExpect(
                        jsonPath("$.codigoUniversitario")
                                .value("EST0000001")
                )
                .andExpect(
                        jsonPath("$.nombres")
                                .value("Ana")
                )
                .andExpect(
                        jsonPath("$.apellidos")
                                .value("Torres")
                )
                .andExpect(
                        jsonPath("$.fechaHoraInicio")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.fechaHoraFin")
                                .exists()
                );

        System.out.println(
                "[PRUEBA CONTROLLER 5 R3] OK - Reserva localizada correctamente."
        );

        System.out.println(
                "[PRUEBA CONTROLLER 5 R3] Ambiente devuelto: AMB-SIM-001"
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe devolver error cuando el usuario no tiene reserva")
    void debeDevolverErrorCuandoUsuarioNoTieneReserva()
            throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 6 R3] Buscando reserva de usuario sin reserva..."
        );

        Usuario usuario = new Usuario();

        usuario.setDni(
                "00000001"
        );

        usuario.setCodigoUniversitario(
                "ADM0000001"
        );

        usuario.setNombres(
                "Administrador"
        );

        usuario.setApellidos(
                "Simulado"
        );

        when(
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "00000001"
                        )
        ).thenReturn(
                usuario
        );

        when(
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        )
        ).thenThrow(
                new IllegalArgumentException(
                        "El usuario no se encuentra asociado a ninguna reserva."
                )
        );

        mockMvc.perform(
                        get(
                                "/api/validaciones-ingreso/manual/reserva"
                        )
                                .param(
                                        "tipo",
                                        "DNI"
                                )
                                .param(
                                        "valor",
                                        "00000001"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "El usuario no se encuentra asociado a ninguna reserva."
                                )
                );

        System.out.println(
                "[PRUEBA CONTROLLER 6 R3] OK - Usuario sin reserva rechazado correctamente."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe confirmar correctamente una validación manual")
    void debeConfirmarValidacionManual() throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 7 R3] Confirmando validación manual..."
        );

        ReservaUsuario reservaUsuario =
                new ReservaUsuario();

        reservaUsuario.setIdReservaUsuario(
                1
        );

        PuntoValidacion puntoValidacion =
                new PuntoValidacion();

        puntoValidacion.setCodigoPunto(
                "PVAL-SIM-001"
        );

        ValidacionIngreso validacion =
                new ValidacionIngreso();

        validacion.setIdValidacion(
                25
        );

        validacion.setReservaUsuario(
                reservaUsuario
        );

        validacion.setPuntoValidacion(
                puntoValidacion
        );

        validacion.setMedioValidacion(
                MedioValidacion.INGRESO_MANUAL
        );

        validacion.setTipoIdentificador(
                TipoIdentificador.DNI
        );

        validacion.setFechaHoraValidacion(
                LocalDateTime.of(
                        2026,
                        9,
                        17,
                        21,
                        30
                )
        );

        when(
                validacionIngresoService
                        .confirmarValidacionManual(
                                1,
                                TipoIdentificador.DNI
                        )
        ).thenReturn(
                validacion
        );

        mockMvc.perform(
                        post(
                                "/api/validaciones-ingreso/manual/confirmar"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "idReservaUsuario": 1,
                                          "tipoIdentificador": "DNI"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.idValidacion")
                                .value(25)
                )
                .andExpect(
                        jsonPath("$.idReservaUsuario")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.medioValidacion")
                                .value("INGRESO_MANUAL")
                )
                .andExpect(
                        jsonPath("$.tipoIdentificador")
                                .value("DNI")
                )
                .andExpect(
                        jsonPath("$.fechaHoraValidacion")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.codigoPunto")
                                .value("PVAL-SIM-001")
                );

        System.out.println(
                "[PRUEBA CONTROLLER 7 R3] OK - Validación manual confirmada correctamente."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe rechazar una validación manual duplicada")
    void debeRechazarValidacionManualDuplicada()
            throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 8 R3] Probando validación manual duplicada..."
        );

        when(
                validacionIngresoService
                        .confirmarValidacionManual(
                                1,
                                TipoIdentificador.DNI
                        )
        ).thenThrow(
                new IllegalArgumentException(
                        "El usuario ya cuenta con una validación aceptada para esta reserva."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/validaciones-ingreso/manual/confirmar"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "idReservaUsuario": 1,
                                          "tipoIdentificador": "DNI"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "El usuario ya cuenta con una validación aceptada para esta reserva."
                                )
                );

        System.out.println(
                "[PRUEBA CONTROLLER 8 R3] OK - Validación duplicada rechazada correctamente."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe registrar correctamente una validación por escaneo simulado")
    void debeRegistrarValidacionPorEscaneoSimulado()
            throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 9 R3] Validando ingreso por escaneo simulado..."
        );

        ReservaUsuario reservaUsuario =
                new ReservaUsuario();

        reservaUsuario.setIdReservaUsuario(
                1
        );

        PuntoValidacion puntoValidacion =
                new PuntoValidacion();

        puntoValidacion.setCodigoPunto(
                "PVAL-SIM-001"
        );

        ValidacionIngreso validacion =
                new ValidacionIngreso();

        validacion.setIdValidacion(
                30
        );

        validacion.setReservaUsuario(
                reservaUsuario
        );

        validacion.setPuntoValidacion(
                puntoValidacion
        );

        validacion.setMedioValidacion(
                MedioValidacion.ESCANEO_SIMULADO
        );

        validacion.setTipoIdentificador(
                TipoIdentificador.DNI
        );

        validacion.setFechaHoraValidacion(
                LocalDateTime.of(
                        2026,
                        9,
                        17,
                        22,
                        0
                )
        );

        when(
                validacionIngresoService
                        .validarIngresoPorEscaneo(
                                "10000001",
                                "PVAL-SIM-001"
                        )
        ).thenReturn(
                validacion
        );

        mockMvc.perform(
                        post(
                                "/api/validaciones-ingreso/escaneo"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "dni": "10000001",
                                          "codigoPunto": "PVAL-SIM-001"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.idValidacion")
                                .value(30)
                )
                .andExpect(
                        jsonPath("$.idReservaUsuario")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.medioValidacion")
                                .value("ESCANEO_SIMULADO")
                )
                .andExpect(
                        jsonPath("$.tipoIdentificador")
                                .value("DNI")
                )
                .andExpect(
                        jsonPath("$.codigoPunto")
                                .value("PVAL-SIM-001")
                )
                .andExpect(
                        jsonPath("$.fechaHoraValidacion")
                                .exists()
                );

        System.out.println(
                "[PRUEBA CONTROLLER 9 R3] OK - Escaneo simulado aceptado correctamente."
        );
    }

    @Test
    @DisplayName("R3 Controller - Debe rechazar el escaneo cuando la reserva corresponde a otro ambiente")
    void debeRechazarEscaneoPorAmbienteIncorrecto()
            throws Exception {

        System.out.println(
                "\n[PRUEBA CONTROLLER 10 R3] Probando escaneo en ambiente incorrecto..."
        );

        when(
                validacionIngresoService
                        .validarIngresoPorEscaneo(
                                "10000001",
                                "PVAL-SIM-999"
                        )
        ).thenThrow(
                new IllegalArgumentException(
                        "La reserva vigente del usuario no corresponde al ambiente."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/validaciones-ingreso/escaneo"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "dni": "10000001",
                                          "codigoPunto": "PVAL-SIM-999"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "La reserva vigente del usuario no corresponde al ambiente."
                                )
                );

        System.out.println(
                "[PRUEBA CONTROLLER 10 R3] OK - Ambiente incorrecto rechazado correctamente."
        );
    }
}