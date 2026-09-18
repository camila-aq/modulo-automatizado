package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        properties = "app.simulation-data.enabled=true"
)
@Transactional
class ValidacionIngresoEscaneoServiceTest {

    @Autowired
    private ValidacionIngresoService validacionIngresoService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaEstadoRepository reservaEstadoRepository;

    @Autowired
    private AmbienteRepository ambienteRepository;

    @Autowired
    private PuntoValidacionRepository puntoValidacionRepository;

    @Test
    @DisplayName("R3 - Debe aceptar una validación de ingreso válida")
    void debeRegistrarValidacionPorEscaneoCuandoLosDatosSonValidos() {

        System.out.println(
                "\n[PRUEBA 1 R3] Iniciando validación válida..."
        );

        ValidacionIngreso validacion =
                validacionIngresoService.validarIngresoPorEscaneo(
                        "10000001",
                        "PVAL-SIM-001"
                );

        assertNotNull(
                validacion,
                "La validación no debería ser nula."
        );

        assertNotNull(
                validacion.getIdValidacion(),
                "La validación debería haber sido persistida."
        );

        assertNotNull(
                validacion.getFechaHoraValidacion(),
                "La validación debería tener fecha y hora."
        );

        assertEquals(
                MedioValidacion.ESCANEO_SIMULADO,
                validacion.getMedioValidacion()
        );

        assertEquals(
                TipoIdentificador.DNI,
                validacion.getTipoIdentificador()
        );

        assertEquals(
                "10000001",
                validacion
                        .getReservaUsuario()
                        .getUsuario()
                        .getDni()
        );

        assertEquals(
                "PVAL-SIM-001",
                validacion
                        .getPuntoValidacion()
                        .getCodigoPunto()
        );

        System.out.println(
                "[PRUEBA 1 R3] OK - Validación aceptada correctamente."
        );

        System.out.println(
                "[PRUEBA 1 R3] ID temporal generado: "
                        + validacion.getIdValidacion()
        );

        System.out.println(
                "[PRUEBA 1 R3] Fecha/hora: "
                        + validacion.getFechaHoraValidacion()
        );
    }

    @Test
    @DisplayName("R3 - Debe rechazar una validación duplicada")
    void debeRechazarValidacionDuplicadaParaLaMismaReserva() {

        System.out.println(
                "\n[PRUEBA 2 R3] Iniciando prueba de duplicado..."
        );

        validacionIngresoService.validarIngresoPorEscaneo(
                "10000001",
                "PVAL-SIM-001"
        );

        System.out.println(
                "[PRUEBA 2 R3] Primera validación aceptada."
        );

        System.out.println(
                "[PRUEBA 2 R3] Intentando validar nuevamente al mismo usuario..."
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService.validarIngresoPorEscaneo(
                                "10000001",
                                "PVAL-SIM-001"
                        )
                );

        assertEquals(
                "El usuario ya cuenta con una validación aceptada para esta reserva.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 2 R3] OK - Validación duplicada rechazada."
        );

        System.out.println(
                "[PRUEBA 2 R3] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 - Debe rechazar un DNI no registrado")
    void debeRechazarDniNoRegistrado() {

        System.out.println(
                "\n[PRUEBA 3 R3] Iniciando prueba con DNI no registrado..."
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService.validarIngresoPorEscaneo(
                                "99999999",
                                "PVAL-SIM-001"
                        )
                );

        assertEquals(
                "El DNI no corresponde a un usuario registrado.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 3 R3] OK - DNI no registrado rechazado correctamente."
        );

        System.out.println(
                "[PRUEBA 3 R3] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 - Debe rechazar un punto de validación inexistente")
    void debeRechazarPuntoDeValidacionInexistente() {

        System.out.println(
                "\n[PRUEBA 4 R3] Iniciando prueba con punto de validación inexistente..."
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService.validarIngresoPorEscaneo(
                                "10000001",
                                "PVAL-NO-EXISTE"
                        )
                );

        assertEquals(
                "El punto de validación no existe o se encuentra inactivo.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 4 R3] OK - Punto de validación inexistente rechazado correctamente."
        );

        System.out.println(
                "[PRUEBA 4 R3] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 - Debe rechazar un usuario registrado sin reserva asociada")
    void debeRechazarUsuarioSinReservaAsociada() {

        System.out.println(
                "\n[PRUEBA 5 R3] Iniciando prueba con usuario registrado sin reserva..."
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService.validarIngresoPorEscaneo(
                                "00000001",
                                "PVAL-SIM-001"
                        )
                );

        assertEquals(
                "El usuario no se encuentra asociado a ninguna reserva.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 5 R3] OK - Usuario sin reserva rechazado correctamente."
        );

        System.out.println(
                "[PRUEBA 5 R3] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 - Debe permitir la validación cerca del final de una reserva vigente")
    void debePermitirValidacionCercaDelFinalDeLaReserva() {

        System.out.println(
                "\n[PRUEBA 6 R3] Iniciando prueba de ingreso tardío..."
        );

        Reserva reserva = reservaRepository
                .findByCodigoReserva("RES-SIM-001")
                .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Simulamos una reserva que empezó hace 50 minutos
         * y termina dentro de 10 minutos.
         *
         * El estudiante llega muy tarde, pero la reserva
         * todavía se encuentra dentro de su horario.
         */

        reserva.setFechaHoraInicio(
                ahora.minusMinutes(50)
        );

        reserva.setFechaHoraFin(
                ahora.plusMinutes(10)
        );

        reservaRepository.saveAndFlush(reserva);

        ValidacionIngreso validacion =
                validacionIngresoService.validarIngresoPorEscaneo(
                        "10000002",
                        "PVAL-SIM-001"
                );

        assertNotNull(validacion);

        assertEquals(
                "10000002",
                validacion
                        .getReservaUsuario()
                        .getUsuario()
                        .getDni()
        );

        System.out.println(
                "[PRUEBA 6 R3] OK - Ingreso tardío aceptado correctamente."
        );

        System.out.println(
                "[PRUEBA 6 R3] La reserva aún tenía 10 minutos disponibles."
        );

        System.out.println(
                "[PRUEBA 6 R3] El control de ocupación no intervino en esta validación."
        );
    }

    @Test
    @DisplayName("R3 - Debe rechazar una validación cuando la reserva ya terminó")
    void debeRechazarValidacionCuandoLaReservaYaTermino() {

        System.out.println(
                "\n[PRUEBA 7 R3] Iniciando prueba con reserva finalizada por horario..."
        );

        Reserva reserva = reservaRepository
                .findByCodigoReserva("RES-SIM-001")
                .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Simulamos que la reserva terminó hace 10 minutos.
         * Aunque el estudiante pertenece a ella y su estado
         * registrado sigue siendo VIGENTE, ya está fuera
         * del intervalo horario permitido para R3.
         */
        reserva.setFechaHoraInicio(
                ahora.minusHours(2)
        );

        reserva.setFechaHoraFin(
                ahora.minusMinutes(10)
        );

        reservaRepository.saveAndFlush(reserva);

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService.validarIngresoPorEscaneo(
                                "10000003",
                                "PVAL-SIM-001"
                        )
                );

        assertEquals(
                "El usuario no se encuentra asociado a una reserva vigente.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 7 R3] OK - Validación rechazada por estar fuera del horario."
        );

        System.out.println(
                "[PRUEBA 7 R3] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 - Debe rechazar una reserva cuyo último estado no es vigente")
    void debeRechazarReservaConEstadoNoVigente() {

        System.out.println(
                "\n[PRUEBA 8 R3] Iniciando prueba con reserva no vigente..."
        );

        Reserva reserva = reservaRepository
                .findByCodigoReserva("RES-SIM-001")
                .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * La reserva continúa dentro de su horario.
         * Lo que cambia es su estado lógico.
         */
        reserva.setFechaHoraInicio(
                ahora.minusMinutes(30)
        );

        reserva.setFechaHoraFin(
                ahora.plusMinutes(30)
        );

        reservaRepository.saveAndFlush(reserva);

        /*
         * No modificamos el estado VIGENTE existente.
         * Registramos un nuevo estado histórico.
         */
        ReservaEstado estadoCancelado = new ReservaEstado();

        estadoCancelado.setReserva(reserva);
        estadoCancelado.setEstadoReserva(
                EstadoReserva.CANCELADA
        );

        estadoCancelado.setMotivo(
                "Estado temporal para prueba interna R3"
        );

        estadoCancelado.setFechaHoraEstado(
                ahora
        );

        estadoCancelado.setFechaCreacion(
                ahora
        );

        estadoCancelado.setUsuarioCreacion(
                reserva.getUsuarioCreacion()
        );

        reservaEstadoRepository.saveAndFlush(
                estadoCancelado
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService.validarIngresoPorEscaneo(
                                "10000004",
                                "PVAL-SIM-001"
                        )
                );

        assertEquals(
                "El usuario no se encuentra asociado a una reserva vigente.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 8 R3] OK - Reserva con estado CANCELADA rechazada correctamente."
        );

        System.out.println(
                "[PRUEBA 8 R3] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 - Debe rechazar una validación en un ambiente diferente al reservado")
    void debeRechazarValidacionEnOtroAmbiente() {

        System.out.println(
                "\n[PRUEBA 9 R3] Iniciando prueba en ambiente diferente..."
        );

        Reserva reserva = reservaRepository
                .findByCodigoReserva("RES-SIM-001")
                .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Creamos temporalmente un segundo ambiente
         * dentro de la misma ubicación.
         */
        Ambiente otroAmbiente = new Ambiente();

        otroAmbiente.setUbicacion(
                reserva.getAmbiente().getUbicacion()
        );

        otroAmbiente.setCodigo(
                "AMB-TEST-R3-002"
        );

        otroAmbiente.setNombre(
                "Sala temporal para prueba R3"
        );

        otroAmbiente.setAbreviatura(
                "TEST-R3"
        );

        otroAmbiente.setPiso(
                "2"
        );

        otroAmbiente.setCantidadMinima(
                2
        );

        otroAmbiente.setCantidadMaxima(
                4
        );

        otroAmbiente.setActivo(
                true
        );

        otroAmbiente.setFechaCreacion(
                ahora
        );

        otroAmbiente.setUsuarioCreacion(
                reserva.getUsuarioCreacion()
        );

        ambienteRepository.saveAndFlush(
                otroAmbiente
        );

        /*
         * Creamos un punto de validación asociado
         * únicamente al segundo ambiente.
         */
        PuntoValidacion otroPunto = new PuntoValidacion();

        otroPunto.setAmbiente(
                otroAmbiente
        );

        otroPunto.setCodigoPunto(
                "PVAL-TEST-R3-002"
        );

        otroPunto.setActivo(
                true
        );

        otroPunto.setFechaCreacion(
                ahora
        );

        otroPunto.setUsuarioCreacion(
                reserva.getUsuarioCreacion()
        );

        puntoValidacionRepository.saveAndFlush(
                otroPunto
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService.validarIngresoPorEscaneo(
                                "10000001",
                                "PVAL-TEST-R3-002"
                        )
                );

        assertEquals(
                "La reserva vigente del usuario no corresponde al ambiente.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 9 R3] OK - Validación en otro ambiente rechazada correctamente."
        );

        System.out.println(
                "[PRUEBA 9 R3] Ambiente reservado: "
                        + reserva.getAmbiente().getCodigo()
        );

        System.out.println(
                "[PRUEBA 9 R3] Ambiente del punto utilizado: "
                        + otroAmbiente.getCodigo()
        );

        System.out.println(
                "[PRUEBA 9 R3] Motivo: "
                        + excepcion.getMessage()
        );
    }
}