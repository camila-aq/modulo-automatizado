package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        properties = "app.simulation-data.enabled=true"
)
@Transactional
class ValidacionIngresoManualServiceTest {

    @Autowired
    private ValidacionIngresoService validacionIngresoService;

    @Autowired
    private ValidacionIngresoRepository validacionIngresoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaEstadoRepository reservaEstadoRepository;

    @Test
    @DisplayName("R3 Manual - Debe identificar correctamente un usuario por DNI")
    void debeIdentificarUsuarioPorDni() {

        System.out.println(
                "\n[PRUEBA 1 R3 MANUAL] Identificando usuario por DNI..."
        );

        Usuario usuario =
                validacionIngresoService.identificarUsuarioPorDni(
                        "10000001"
                );

        assertNotNull(usuario);

        assertEquals(
                "10000001",
                usuario.getDni()
        );

        assertEquals(
                "Ana",
                usuario.getNombres()
        );

        assertEquals(
                "Torres",
                usuario.getApellidos()
        );

        System.out.println(
                "[PRUEBA 1 R3 MANUAL] OK - Usuario identificado correctamente."
        );

        System.out.println(
                "[PRUEBA 1 R3 MANUAL] Usuario: "
                        + usuario.getNombres()
                        + " "
                        + usuario.getApellidos()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe identificar correctamente un usuario por código universitario")
    void debeIdentificarUsuarioPorCodigoUniversitario() {

        System.out.println(
                "\n[PRUEBA 2 R3 MANUAL] Identificando usuario por código universitario..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorCodigoUniversitario(
                                "EST0000001"
                        );

        assertNotNull(usuario);

        assertEquals(
                "EST0000001",
                usuario.getCodigoUniversitario()
        );

        assertEquals(
                "Ana",
                usuario.getNombres()
        );

        assertEquals(
                "Torres",
                usuario.getApellidos()
        );

        System.out.println(
                "[PRUEBA 2 R3 MANUAL] OK - Usuario identificado correctamente."
        );

        System.out.println(
                "[PRUEBA 2 R3 MANUAL] Usuario: "
                        + usuario.getNombres()
                        + " "
                        + usuario.getApellidos()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe rechazar un DNI no registrado")
    void debeRechazarDniNoRegistrado() {

        System.out.println(
                "\n[PRUEBA 3 R3 MANUAL] Identificando DNI no registrado..."
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService
                                .identificarUsuarioPorDni(
                                        "99999999"
                                )
                );

        assertEquals(
                "El DNI no corresponde a un usuario registrado.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 3 R3 MANUAL] OK - DNI no registrado rechazado correctamente."
        );

        System.out.println(
                "[PRUEBA 3 R3 MANUAL] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe rechazar un código universitario no registrado")
    void debeRechazarCodigoUniversitarioNoRegistrado() {

        System.out.println(
                "\n[PRUEBA 4 R3 MANUAL] Identificando código universitario no registrado..."
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService
                                .identificarUsuarioPorCodigoUniversitario(
                                        "EST9999999"
                                )
                );

        assertEquals(
                "El código universitario no corresponde a un usuario registrado.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 4 R3 MANUAL] OK - Código universitario no registrado rechazado correctamente."
        );

        System.out.println(
                "[PRUEBA 4 R3 MANUAL] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe localizar la reserva vigente sin registrar el ingreso")
    void debeLocalizarReservaVigenteSinRegistrarIngreso() {

        System.out.println(
                "\n[PRUEBA 5 R3 MANUAL] Buscando reserva vigente del usuario..."
        );

        Usuario usuario =
                validacionIngresoService.identificarUsuarioPorDni(
                        "10000001"
                );

        ReservaUsuario reservaUsuario =
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        );

        assertNotNull(reservaUsuario);

        assertEquals(
                "10000001",
                reservaUsuario.getUsuario().getDni()
        );

        assertEquals(
                "RES-SIM-001",
                reservaUsuario.getReserva().getCodigoReserva()
        );

        assertEquals(
                "AMB-SIM-001",
                reservaUsuario
                        .getReserva()
                        .getAmbiente()
                        .getCodigo()
        );

        assertFalse(
                validacionIngresoRepository.existsByReservaUsuario(
                        reservaUsuario
                )
        );

        System.out.println(
                "[PRUEBA 5 R3 MANUAL] OK - Reserva localizada correctamente."
        );

        System.out.println(
                "[PRUEBA 5 R3 MANUAL] Reserva: "
                        + reservaUsuario
                        .getReserva()
                        .getCodigoReserva()
        );

        System.out.println(
                "[PRUEBA 5 R3 MANUAL] Ambiente: "
                        + reservaUsuario
                        .getReserva()
                        .getAmbiente()
                        .getCodigo()
        );

        System.out.println(
                "[PRUEBA 5 R3 MANUAL] OK - La búsqueda no registró el ingreso."
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe rechazar un usuario identificado sin reserva asociada")
    void debeRechazarUsuarioSinReservaAsociada() {

        System.out.println(
                "\n[PRUEBA 6 R3 MANUAL] Buscando reserva de usuario sin reserva asociada..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "00000001"
                        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService
                                .buscarReservaVigenteParaValidacionManual(
                                        usuario
                                )
                );

        assertEquals(
                "El usuario no se encuentra asociado a ninguna reserva.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 6 R3 MANUAL] OK - Usuario sin reserva rechazado correctamente."
        );

        System.out.println(
                "[PRUEBA 6 R3 MANUAL] Usuario identificado: "
                        + usuario.getNombres()
                        + " "
                        + usuario.getApellidos()
        );

        System.out.println(
                "[PRUEBA 6 R3 MANUAL] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe rechazar una reserva que ya terminó")
    void debeRechazarReservaFueraDeHorario() {

        System.out.println(
                "\n[PRUEBA 7 R3 MANUAL] Buscando reserva fuera de horario..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000002"
                        );

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(
                                "RES-SIM-001"
                        )
                        .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        reserva.setFechaHoraInicio(
                ahora.minusHours(2)
        );

        reserva.setFechaHoraFin(
                ahora.minusMinutes(10)
        );

        reservaRepository.saveAndFlush(
                reserva
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService
                                .buscarReservaVigenteParaValidacionManual(
                                        usuario
                                )
                );

        assertEquals(
                "El usuario no se encuentra asociado a una reserva vigente.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 7 R3 MANUAL] OK - Reserva fuera de horario rechazada correctamente."
        );

        System.out.println(
                "[PRUEBA 7 R3 MANUAL] Usuario identificado: "
                        + usuario.getNombres()
                        + " "
                        + usuario.getApellidos()
        );

        System.out.println(
                "[PRUEBA 7 R3 MANUAL] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe rechazar una reserva cuyo último estado no es vigente")
    void debeRechazarReservaConEstadoNoVigente() {

        System.out.println(
                "\n[PRUEBA 8 R3 MANUAL] Buscando reserva con estado no vigente..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000003"
                        );

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(
                                "RES-SIM-001"
                        )
                        .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Mantenemos la reserva dentro del horario
         * para aislar específicamente la validación del estado.
         */
        reserva.setFechaHoraInicio(
                ahora.minusMinutes(30)
        );

        reserva.setFechaHoraFin(
                ahora.plusMinutes(30)
        );

        reservaRepository.saveAndFlush(
                reserva
        );

        /*
         * Se agrega un nuevo estado histórico.
         * No se modifica el estado VIGENTE anterior.
         */
        ReservaEstado estadoCancelado = new ReservaEstado();

        estadoCancelado.setReserva(
                reserva
        );

        estadoCancelado.setEstadoReserva(
                EstadoReserva.CANCELADA
        );

        estadoCancelado.setMotivo(
                "Estado temporal para prueba interna R3 manual"
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
                        () -> validacionIngresoService
                                .buscarReservaVigenteParaValidacionManual(
                                        usuario
                                )
                );

        assertEquals(
                "El usuario no se encuentra asociado a una reserva vigente.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 8 R3 MANUAL] OK - Reserva con estado CANCELADA rechazada correctamente."
        );

        System.out.println(
                "[PRUEBA 8 R3 MANUAL] Usuario identificado: "
                        + usuario.getNombres()
                        + " "
                        + usuario.getApellidos()
        );

        System.out.println(
                "[PRUEBA 8 R3 MANUAL] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe permitir buscar la reserva aunque falten pocos minutos para terminar")
    void debePermitirBusquedaConIngresoTardio() {

        System.out.println(
                "\n[PRUEBA 9 R3 MANUAL] Buscando reserva con ingreso tardío..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000004"
                        );

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(
                                "RES-SIM-001"
                        )
                        .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        reserva.setFechaHoraInicio(
                ahora.minusMinutes(50)
        );

        reserva.setFechaHoraFin(
                ahora.plusMinutes(10)
        );

        reservaRepository.saveAndFlush(
                reserva
        );

        ReservaUsuario reservaUsuario =
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        );

        assertNotNull(
                reservaUsuario
        );

        assertEquals(
                "RES-SIM-001",
                reservaUsuario
                        .getReserva()
                        .getCodigoReserva()
        );

        assertEquals(
                "10000004",
                reservaUsuario
                        .getUsuario()
                        .getDni()
        );

        assertFalse(
                validacionIngresoRepository.existsByReservaUsuario(
                        reservaUsuario
                )
        );

        System.out.println(
                "[PRUEBA 9 R3 MANUAL] OK - Reserva localizada aunque faltaban 10 minutos para terminar."
        );

        System.out.println(
                "[PRUEBA 9 R3 MANUAL] El control de ocupación no intervino en la búsqueda."
        );

        System.out.println(
                "[PRUEBA 9 R3 MANUAL] OK - La búsqueda todavía no registró el ingreso."
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe confirmar correctamente una validación manual por DNI")
    void debeConfirmarValidacionManualPorDni() {

        System.out.println(
                "\n[PRUEBA 10 R3 MANUAL] Confirmando validación manual por DNI..."
        );

        /*
         * 1. El administrador identifica al usuario.
         */
        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000001"
                        );

        /*
         * 2. El administrador pulsa "Buscar reserva".
         * Todavía no se registra el ingreso.
         */
        ReservaUsuario reservaUsuario =
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        );

        assertFalse(
                validacionIngresoRepository.existsByReservaUsuario(
                        reservaUsuario
                )
        );

        /*
         * 3. El administrador verifica manualmente
         * la identidad y confirma el ingreso.
         */
        ValidacionIngreso validacion =
                validacionIngresoService
                        .confirmarValidacionManual(
                                reservaUsuario,
                                TipoIdentificador.DNI
                        );

        assertNotNull(
                validacion
        );

        assertNotNull(
                validacion.getIdValidacion()
        );

        assertNotNull(
                validacion.getFechaHoraValidacion()
        );

        assertEquals(
                MedioValidacion.INGRESO_MANUAL,
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
                "RES-SIM-001",
                validacion
                        .getReservaUsuario()
                        .getReserva()
                        .getCodigoReserva()
        );

        assertEquals(
                "PVAL-SIM-001",
                validacion
                        .getPuntoValidacion()
                        .getCodigoPunto()
        );

        System.out.println(
                "[PRUEBA 10 R3 MANUAL] OK - Validación manual registrada correctamente."
        );

        System.out.println(
                "[PRUEBA 10 R3 MANUAL] Medio: "
                        + validacion.getMedioValidacion()
        );

        System.out.println(
                "[PRUEBA 10 R3 MANUAL] Tipo de identificador: "
                        + validacion.getTipoIdentificador()
        );

        System.out.println(
                "[PRUEBA 10 R3 MANUAL] Punto determinado automáticamente: "
                        + validacion
                        .getPuntoValidacion()
                        .getCodigoPunto()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe confirmar correctamente una validación manual por código universitario")
    void debeConfirmarValidacionManualPorCodigoUniversitario() {

        System.out.println(
                "\n[PRUEBA 11 R3 MANUAL] Confirmando validación manual por código universitario..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorCodigoUniversitario(
                                "EST0000002"
                        );

        ReservaUsuario reservaUsuario =
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        );

        assertFalse(
                validacionIngresoRepository.existsByReservaUsuario(
                        reservaUsuario
                )
        );

        ValidacionIngreso validacion =
                validacionIngresoService
                        .confirmarValidacionManual(
                                reservaUsuario,
                                TipoIdentificador.CODIGO_UNIVERSITARIO
                        );

        assertNotNull(
                validacion
        );

        assertNotNull(
                validacion.getIdValidacion()
        );

        assertEquals(
                MedioValidacion.INGRESO_MANUAL,
                validacion.getMedioValidacion()
        );

        assertEquals(
                TipoIdentificador.CODIGO_UNIVERSITARIO,
                validacion.getTipoIdentificador()
        );

        assertEquals(
                "EST0000002",
                validacion
                        .getReservaUsuario()
                        .getUsuario()
                        .getCodigoUniversitario()
        );

        assertEquals(
                "RES-SIM-001",
                validacion
                        .getReservaUsuario()
                        .getReserva()
                        .getCodigoReserva()
        );

        assertEquals(
                "PVAL-SIM-001",
                validacion
                        .getPuntoValidacion()
                        .getCodigoPunto()
        );

        System.out.println(
                "[PRUEBA 11 R3 MANUAL] OK - Validación manual registrada correctamente."
        );

        System.out.println(
                "[PRUEBA 11 R3 MANUAL] Medio: "
                        + validacion.getMedioValidacion()
        );

        System.out.println(
                "[PRUEBA 11 R3 MANUAL] Tipo de identificador: "
                        + validacion.getTipoIdentificador()
        );

        System.out.println(
                "[PRUEBA 11 R3 MANUAL] Usuario: "
                        + validacion
                        .getReservaUsuario()
                        .getUsuario()
                        .getNombres()
                        + " "
                        + validacion
                        .getReservaUsuario()
                        .getUsuario()
                        .getApellidos()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe rechazar una validación manual duplicada")
    void debeRechazarValidacionManualDuplicada() {

        System.out.println(
                "\n[PRUEBA 12 R3 MANUAL] Intentando registrar una validación manual duplicada..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000003"
                        );

        ReservaUsuario reservaUsuario =
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        );

        /*
         * Primera validación: debe ser aceptada.
         */
        ValidacionIngreso primeraValidacion =
                validacionIngresoService
                        .confirmarValidacionManual(
                                reservaUsuario,
                                TipoIdentificador.DNI
                        );

        assertNotNull(
                primeraValidacion
        );

        System.out.println(
                "[PRUEBA 12 R3 MANUAL] Primera validación aceptada."
        );

        /*
         * Segunda validación para el mismo integrante
         * y la misma reserva: debe ser rechazada.
         */
        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService
                                .confirmarValidacionManual(
                                        reservaUsuario,
                                        TipoIdentificador.DNI
                                )
                );

        assertEquals(
                "El usuario ya cuenta con una validación aceptada para esta reserva.",
                excepcion.getMessage()
        );

        System.out.println(
                "[PRUEBA 12 R3 MANUAL] OK - Validación duplicada rechazada correctamente."
        );

        System.out.println(
                "[PRUEBA 12 R3 MANUAL] Motivo: "
                        + excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("R3 Manual - Debe rechazar la confirmación si la reserva termina después de haber sido localizada")
    void debeRechazarConfirmacionSiReservaTerminaDespuesDeBusqueda() {

        System.out.println(
                "\n[PRUEBA 13 R3 MANUAL] Buscando reserva antes de que termine..."
        );

        Usuario usuario =
                validacionIngresoService
                        .identificarUsuarioPorDni(
                                "10000004"
                        );

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(
                                "RES-SIM-001"
                        )
                        .orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Primero dejamos la reserva vigente para que
         * la búsqueda pueda encontrarla correctamente.
         */
        reserva.setFechaHoraInicio(
                ahora.minusMinutes(30)
        );

        reserva.setFechaHoraFin(
                ahora.plusMinutes(10)
        );

        reservaRepository.saveAndFlush(
                reserva
        );

        ReservaUsuario reservaUsuario =
                validacionIngresoService
                        .buscarReservaVigenteParaValidacionManual(
                                usuario
                        );

        assertNotNull(
                reservaUsuario
        );

        System.out.println(
                "[PRUEBA 13 R3 MANUAL] Reserva localizada correctamente."
        );

        /*
         * Simulamos que la reserva terminó antes
         * de que el administrador confirme el ingreso.
         */
        LocalDateTime momentoPosterior =
                LocalDateTime.now();

        reserva.setFechaHoraInicio(
                momentoPosterior.minusHours(1)
        );

        reserva.setFechaHoraFin(
                momentoPosterior.minusMinutes(1)
        );

        reservaRepository.saveAndFlush(
                reserva
        );

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validacionIngresoService
                                .confirmarValidacionManual(
                                        reservaUsuario,
                                        TipoIdentificador.DNI
                                )
                );

        assertEquals(
                "El usuario no se encuentra asociado a una reserva vigente.",
                excepcion.getMessage()
        );

        assertFalse(
                validacionIngresoRepository.existsByReservaUsuario(
                        reservaUsuario
                )
        );

        System.out.println(
                "[PRUEBA 13 R3 MANUAL] OK - Confirmación rechazada porque la reserva ya había terminado."
        );

        System.out.println(
                "[PRUEBA 13 R3 MANUAL] Motivo: "
                        + excepcion.getMessage()
        );
    }
}
