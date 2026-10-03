package com.camila.moduloautomatizado.service.functional;

import com.camila.moduloautomatizado.dto.ControlOcupacionResponse;
import com.camila.moduloautomatizado.dto.EventoTrazabilidadResponse;
import com.camila.moduloautomatizado.dto.TrazabilidadReservaResponse;

import com.camila.moduloautomatizado.event.ValidacionIngresoRegistradaEvent;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.CorreoElectronico;
import com.camila.moduloautomatizado.model.entity.EvidenciaPrueba;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.RolEnReserva;
import com.camila.moduloautomatizado.model.enums.TipoCorreo;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;

import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.CorreoElectronicoRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import com.camila.moduloautomatizado.service.ControlOcupacionConsultaService;
import com.camila.moduloautomatizado.service.ControlOcupacionOrquestadorService;
import com.camila.moduloautomatizado.service.TrazabilidadReservaService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(
        properties = {
                "app.test-base-data.enabled=true",
                "app.integration-reservation-data.enabled=false",
                "app.mail.enabled=true",
                "spring.jpa.show-sql=false"
        }
)
class ControlOcupacionCasoUsoTest {

    private static final String CODIGO_AMBIENTE = "CCSS-AMB-001";

    private static final List<String> DNIS_INTEGRANTES =
            List.of(
                    "10000001",
                    "10000002",
                    "10000003"
            );

    private static final long TIEMPO_MAXIMO_ESPERA_MS = 5000;
    private static final long INTERVALO_CONSULTA_MS = 100;


    @Autowired
    private AmbienteRepository ambienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaUsuarioRepository reservaUsuarioRepository;

    @Autowired
    private ReservaEstadoRepository reservaEstadoRepository;

    @Autowired
    private PuntoValidacionRepository puntoValidacionRepository;

    @Autowired
    private ValidacionIngresoRepository validacionIngresoRepository;

    @Autowired
    private ControlOcupacionRepository controlOcupacionRepository;

    @Autowired
    private OcupacionEstadoRepository ocupacionEstadoRepository;

    @Autowired
    private CorreoElectronicoRepository correoElectronicoRepository;

    @Autowired
    private ControlOcupacionOrquestadorService controlOcupacionOrquestadorService;

    @Autowired
    private ControlOcupacionConsultaService controlOcupacionConsultaService;

    @Autowired
    private TrazabilidadReservaService trazabilidadReservaService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;


    private final List<Integer> reservasCreadas = new ArrayList<>();

    
    void limpiarDatosDePrueba() {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(
                status -> {

                    for (Integer idReserva : new ArrayList<>(reservasCreadas)) {

                        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);

                        if (reserva == null) {
                            continue;
                        }

                        correoElectronicoRepository.deleteAll(
                                correoElectronicoRepository
                                        .findByReservaOrderByFechaEnvioAsc(reserva)
                        );

                        controlOcupacionRepository
                                .findByReserva(reserva)
                                .ifPresent(control -> {

                                    ocupacionEstadoRepository.deleteAll(
                                            ocupacionEstadoRepository
                                                    .findByControlOcupacionOrderByFechaHoraEstadoAsc(control)
                                    );

                                    controlOcupacionRepository.delete(control);
                                });

                        validacionIngresoRepository.deleteAll(
                                validacionIngresoRepository
                                        .findValidacionesIngresoPorReserva(reserva)
                        );

                        reservaEstadoRepository.deleteAll(
                                reservaEstadoRepository
                                        .findAll()
                                        .stream()
                                        .filter(estado ->
                                                Objects.equals(
                                                        estado.getReserva().getIdReserva(),
                                                        idReserva
                                                )
                                        )
                                        .toList()
                        );

                        reservaUsuarioRepository.deleteAll(
                                reservaUsuarioRepository
                                        .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(reserva)
                        );

                        reservaRepository.delete(reserva);
                    }
                }
        );

        reservasCreadas.clear();
    }


    // CP-R5-CU01
    @Test
    @DisplayName("CP-R5-CU01 - Cantidad mínima no alcanzada durante el periodo de tolerancia")
    void debeMantenerPendienteCuandoNoSeAlcanzaCantidadMinima() {

        String codigoCaso = "CP-R5-CU01";
        String accion = "Ejecutar el control automático con dos usuarios únicos validados.";
        String resultadoEsperado = "Resultado esperado: El módulo mantiene el estado PENDIENTE " +
                "porque la cantidad mínima aún no fue alcanzada y la tolerancia continúa vigente.\n\n" +
                "Objetivo: Verificar que no se produzca un cambio de estado antes de alcanzar " +
                "la cantidad mínima o finalizar el periodo de tolerancia.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Cantidad mínima no alcanzada durante el periodo de tolerancia"
        );

        try {

            EscenarioR5 escenario = crearEscenario(codigoCaso);

            registrarValidacion(
                    escenario.integrantes().get(0),
                    escenario.inicio().plusMinutes(1)
            );

            registrarValidacion(
                    escenario.integrantes().get(1),
                    escenario.inicio().plusMinutes(2)
            );

            precondiciones =
                    """
                    Reserva: %s
                    Ambiente: %s
                    Cantidad mínima requerida: 3
                    Usuarios únicos validados: 2
                    Periodo de tolerancia vigente
                    """.formatted(
                            escenario.reserva().getCodigoReserva(),
                            escenario.reserva().getAmbiente().getCodigo()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.inicio().plusMinutes(5)
                    );

            List<OcupacionEstado> estados = obtenerEstadosAutomaticos(escenario.reserva());

            assertEquals(
                    1,
                    estados.size(),
                    "Se registró un cambio de estado adicional."
            );

            assertEquals(
                    EstadoOcupacion.PENDIENTE,
                    estados.get(0).getEstadoOcupacion(),
                    "La reserva no permaneció PENDIENTE."
            );

            assertEquals(
                    2L,
                    validacionIngresoRepository
                            .contarUsuariosValidadosPorReserva(escenario.reserva()),
                    "El conteo de usuarios validados no corresponde."
            );

            assertTrue(
                    correoElectronicoRepository
                            .findByReservaOrderByFechaEnvioAsc(escenario.reserva())
                            .isEmpty(),
                    "Se registró una comunicación sin existir un cambio terminal."
            );

            String resultadoReal =
                    """
                    Usuarios únicos validados: 2
                    Cantidad mínima requerida: 3
                    Estado de ocupación: PENDIENTE
                    No se registraron cambios adicionales ni comunicaciones.
                    """.trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU02
    @Test
    @DisplayName("CP-R5-CU02 - Evaluación automática ante una nueva validación aceptada")
    void debeEjecutarControlAutomaticamenteAnteNuevaValidacion() {

        String codigoCaso = "CP-R5-CU02";
        String accion = "Registrar una nueva validación aceptada y generar el evento asociado.";
        String resultadoEsperado = "Resultado esperado: El módulo ejecuta automáticamente el control " +
                "de ocupación y considera la nueva validación en el conteo de usuarios únicos.\n\n" +
                "Objetivo: Verificar que una nueva validación aceptada active el control de ocupación.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Evaluación automática ante una nueva validación aceptada"
        );

        try {

            EscenarioR5 escenario = crearEscenario(codigoCaso);

            registrarValidacion(
                    escenario.integrantes().get(0),
                    escenario.inicio().plusMinutes(1)
            );

            precondiciones =
                    """
                    Reserva: %s
                    Estado previo sin control registrado
                    Una validación aceptada disponible
                    Periodo de tolerancia vigente
                    """.formatted(
                            escenario.reserva().getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            registrarValidacionConEvento(
                    escenario.integrantes().get(1),
                    escenario.inicio().plusMinutes(2)
            );

            EstadoOcupacion estado =
                    esperarEstado(
                            escenario.reserva(),
                            EstadoOcupacion.PENDIENTE
                    );

            assertEquals(
                    EstadoOcupacion.PENDIENTE,
                    estado,
                    "El control automático no dejó la reserva en PENDIENTE."
            );

            assertEquals(
                    2L,
                    validacionIngresoRepository
                            .contarUsuariosValidadosPorReserva(escenario.reserva()),
                    "La nueva validación no fue considerada en el conteo."
            );

            assertTrue(
                    controlOcupacionRepository
                            .findByReserva(escenario.reserva())
                            .isPresent(),
                    "No se creó el control de ocupación automáticamente."
            );

            String resultadoReal =
                    """
                    La nueva validación fue registrada.
                    Se ejecutó automáticamente el control de ocupación.
                    Usuarios únicos considerados: 2
                    Estado resultante: PENDIENTE
                    """.trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU03
    @Test
    @DisplayName("CP-R5-CU03 - Conteo de usuarios únicos validados")
    void debeContarUsuariosValidadosSinDuplicados() {

        String codigoCaso = "CP-R5-CU03";
        String accion = "Registrar dos validaciones asociadas al mismo usuario y ejecutar el conteo.";
        String resultadoEsperado = "Resultado esperado: El módulo contabiliza al usuario una sola " +
                "vez al determinar la cantidad de usuarios únicos correctamente validados.\n\n" +
                "Objetivo: Verificar que registros duplicados de un mismo usuario no aumenten " +
                "incorrectamente el conteo utilizado para determinar la ocupación.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Conteo de usuarios únicos validados"
        );

        try {

            EscenarioR5 escenario = crearEscenario(codigoCaso);

            ReservaUsuario asociacionOriginal = escenario.integrantes().get(0);

            ReservaUsuario asociacionDuplicada =
                    crearAsociacionDuplicada(
                            escenario.reserva(),
                            asociacionOriginal.getUsuario()
                    );

            registrarValidacion(
                    asociacionOriginal,
                    escenario.inicio().plusMinutes(1)
            );

            registrarValidacion(
                    asociacionDuplicada,
                    escenario.inicio().plusMinutes(2)
            );

            precondiciones =
                    """
                    Reserva: %s
                    Dos registros de validación disponibles
                    Ambos registros corresponden al mismo usuario
                    Cantidad mínima requerida: 3
                    """.formatted(
                            escenario.reserva().getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            long usuariosUnicos =
                    validacionIngresoRepository
                            .contarUsuariosValidadosPorReserva(escenario.reserva());

            assertEquals(
                    1L,
                    usuariosUnicos,
                    "El mismo usuario fue contabilizado más de una vez."
            );

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.inicio().plusMinutes(5)
                    );

            assertEquals(
                    EstadoOcupacion.PENDIENTE,
                    obtenerEstadoActual(escenario.reserva()),
                    "El conteo duplicado produjo un cambio de estado incorrecto."
            );

            String resultadoReal =
                    """
                    Registros de validación considerados: 2
                    Usuario asociado a ambos registros: el mismo
                    Usuarios únicos contabilizados: 1
                    Estado resultante: PENDIENTE
                    """.trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU04
    @Test
    @DisplayName("CP-R5-CU04 - Cambio automático a estado OCUPADO")
    void debeCambiarAutomaticamenteAOcupado() {

        String codigoCaso = "CP-R5-CU04";
        String accion = "Ejecutar el control después de alcanzar la cantidad mínima requerida.";
        String resultadoEsperado = "Resultado esperado: El módulo registra un nuevo estado OCUPADO " +
                "con su fecha y hora y conserva el estado PENDIENTE previamente registrado.\n\n" +
                "Objetivo: Verificar el cambio automático a OCUPADO cuando se alcanza el mínimo " +
                "dentro del periodo de tolerancia.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Cambio automático a estado OCUPADO"
        );

        try {

            EscenarioR5 escenario = crearEscenario(codigoCaso);

            registrarTresValidaciones(escenario);

            LocalDateTime momento = escenario.inicio().plusMinutes(5);

            precondiciones =
                    """
                    Reserva: %s
                    Cantidad mínima requerida: 3
                    Usuarios únicos validados: 3
                    Periodo de tolerancia vigente
                    """.formatted(
                            escenario.reserva().getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            momento
                    );

            List<OcupacionEstado> estados = obtenerEstadosAutomaticos(escenario.reserva());

            assertEquals(
                    2,
                    estados.size(),
                    "El historial no contiene los dos estados esperados."
            );

            assertEquals(
                    EstadoOcupacion.PENDIENTE,
                    estados.get(0).getEstadoOcupacion(),
                    "No se conservó el estado PENDIENTE previo."
            );

            assertEquals(
                    EstadoOcupacion.OCUPADO,
                    estados.get(1).getEstadoOcupacion(),
                    "No se registró el estado OCUPADO."
            );

            assertEquals(
                    momento,
                    estados.get(1).getFechaHoraEstado(),
                    "La fecha y hora del cambio no corresponde."
            );

            String resultadoReal =
                    """
                    Cantidad mínima requerida: 3
                    Usuarios únicos validados: 3
                    Estado previo conservado: PENDIENTE
                    Nuevo estado registrado: OCUPADO
                    Fecha y hora del cambio: %s
                    """.formatted(
                            estados.get(1).getFechaHoraEstado()
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU05
    @Test
    @DisplayName("CP-R5-CU05 - Liberación automática al finalizar el periodo de tolerancia")
    void debeLiberarAutomaticamenteAlFinalizarTolerancia() {

        String codigoCaso = "CP-R5-CU05";
        String accion = "Ejecutar la evaluación automática al alcanzar el límite del periodo de tolerancia.";
        String resultadoEsperado = "Resultado esperado: El módulo registra un nuevo estado LIBERADO " +
                "con la fecha y hora correspondiente, sin intervención administrativa.\n\n" +
                "Objetivo: Verificar la liberación automática cuando finaliza la tolerancia " +
                "sin alcanzarse la cantidad mínima requerida.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Liberación automática al finalizar el periodo de tolerancia"
        );

        try {

            EscenarioR5 escenario = crearEscenario(codigoCaso);

            registrarValidacion(
                    escenario.integrantes().get(0),
                    escenario.inicio().plusMinutes(1)
            );

            registrarValidacion(
                    escenario.integrantes().get(1),
                    escenario.inicio().plusMinutes(2)
            );

            precondiciones =
                    """
                    Reserva: %s
                    Cantidad mínima requerida: 3
                    Usuarios únicos validados: 2
                    Fecha límite de tolerancia: %s
                    """.formatted(
                            escenario.reserva().getCodigoReserva(),
                            escenario.limite()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.limite()
                    );

            List<OcupacionEstado> estados = obtenerEstadosAutomaticos(escenario.reserva());

            assertEquals(
                    2,
                    estados.size(),
                    "El historial no contiene los estados esperados."
            );

            assertEquals(
                    EstadoOcupacion.PENDIENTE,
                    estados.get(0).getEstadoOcupacion(),
                    "No se conservó el estado PENDIENTE."
            );

            assertEquals(
                    EstadoOcupacion.LIBERADO,
                    estados.get(1).getEstadoOcupacion(),
                    "No se registró el estado LIBERADO."
            );

            assertEquals(
                    escenario.limite(),
                    estados.get(1).getFechaHoraEstado(),
                    "La liberación no quedó registrada en el instante esperado."
            );

            String resultadoReal =
                    """
                    Cantidad mínima requerida: 3
                    Usuarios únicos validados: 2
                    Estado previo conservado: PENDIENTE
                    Nuevo estado registrado: LIBERADO
                    Fecha y hora de liberación: %s
                    """.formatted(
                            estados.get(1).getFechaHoraEstado()
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal = "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU06
    @Test
    @DisplayName("CP-R5-CU06 - Reserva previamente ocupada")
    void debeMantenerReservaPreviamenteOcupada() {

        String codigoCaso = "CP-R5-CU06";
        String accion = "Ejecutar nuevamente el control sobre una reserva cuyo estado actual es OCUPADO.";
        String resultadoEsperado = "Resultado esperado: El módulo mantiene el estado OCUPADO " +
                "y no genera un nuevo estado ni una nueva comunicación.\n\n" +
                "Objetivo: Verificar que los estados terminales no sean procesados nuevamente.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Reserva previamente ocupada"
        );

        try {

            EscenarioR5 escenario = crearEscenario(codigoCaso);

            registrarTresValidaciones(escenario);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.inicio().plusMinutes(5)
                    );

            int estadosAntes = obtenerEstadosAutomaticos(escenario.reserva()).size();

            int correosAntes =
                    correoElectronicoRepository
                            .findByReservaOrderByFechaEnvioAsc(escenario.reserva())
                            .size();

            precondiciones =
                    """
                    Reserva: %s
                    Estado de ocupación actual: OCUPADO
                    Estados registrados antes de la reevaluación: %d
                    Comunicaciones registradas antes de la reevaluación: %d
                    """.formatted(
                            escenario.reserva().getCodigoReserva(),
                            estadosAntes,
                            correosAntes
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.inicio().plusMinutes(6)
                    );

            int estadosDespues =
                    obtenerEstadosAutomaticos(escenario.reserva()).size();

            int correosDespues =
                    correoElectronicoRepository
                            .findByReservaOrderByFechaEnvioAsc(escenario.reserva())
                            .size();

            assertEquals(
                    EstadoOcupacion.OCUPADO,
                    obtenerEstadoActual(escenario.reserva()),
                    "El estado OCUPADO fue modificado."
            );

            assertEquals(
                    estadosAntes,
                    estadosDespues,
                    "Se registró un estado adicional."
            );

            assertEquals(
                    correosAntes,
                    correosDespues,
                    "Se registró una comunicación adicional."
            );

            String resultadoReal =
                    """
                    Estado final: OCUPADO
                    Estados antes de reevaluar: %d
                    Estados después de reevaluar: %d
                    Comunicaciones antes de reevaluar: %d
                    Comunicaciones después de reevaluar: %d
                    """.formatted(
                            estadosAntes,
                            estadosDespues,
                            correosAntes,
                            correosDespues
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU07
    @Test
    @DisplayName("CP-R5-CU07 - Reserva previamente liberada")
    void debeMantenerReservaPreviamenteLiberada() {

        String codigoCaso = "CP-R5-CU07";
        String accion = "Ejecutar nuevamente el control sobre una reserva cuyo estado actual es LIBERADO.";
        String resultadoEsperado = "Resultado esperado: El módulo mantiene el estado LIBERADO " +
                "y no genera modificaciones adicionales.\n\n" +
                "Objetivo: Verificar que una reserva liberada no vuelva a ser procesada.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Reserva previamente liberada"
        );

        try {

            EscenarioR5 escenario =
                    crearEscenario(codigoCaso);

            registrarValidacion(
                    escenario.integrantes().get(0),
                    escenario.inicio().plusMinutes(1)
            );

            registrarValidacion(
                    escenario.integrantes().get(1),
                    escenario.inicio().plusMinutes(2)
            );

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.limite()
                    );

            int estadosAntes =
                    obtenerEstadosAutomaticos(escenario.reserva()).size();

            int correosAntes =
                    correoElectronicoRepository
                            .findByReservaOrderByFechaEnvioAsc(escenario.reserva())
                            .size();

            precondiciones =
                    """
                    Reserva: %s
                    Estado de ocupación actual: LIBERADO
                    Estados registrados antes de la reevaluación: %d
                    Comunicaciones registradas antes de la reevaluación: %d
                    """.formatted(
                            escenario.reserva().getCodigoReserva(),
                            estadosAntes,
                            correosAntes
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.limite().plusMinutes(1)
                    );

            int estadosDespues =
                    obtenerEstadosAutomaticos(escenario.reserva()).size();

            int correosDespues =
                    correoElectronicoRepository
                            .findByReservaOrderByFechaEnvioAsc(escenario.reserva())
                            .size();

            assertEquals(
                    EstadoOcupacion.LIBERADO,
                    obtenerEstadoActual(escenario.reserva()),
                    "El estado LIBERADO fue modificado."
            );

            assertEquals(
                    estadosAntes,
                    estadosDespues,
                    "Se registró un estado adicional."
            );

            assertEquals(
                    correosAntes,
                    correosDespues,
                    "Se registró una comunicación adicional."
            );

            String resultadoReal =
                    """
                    Estado final: LIBERADO
                    Estados antes de reevaluar: %d
                    Estados después de reevaluar: %d
                    Comunicaciones antes de reevaluar: %d
                    Comunicaciones después de reevaluar: %d
                    """.formatted(
                            estadosAntes,
                            estadosDespues,
                            correosAntes,
                            correosDespues
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU08
    @Test
    @DisplayName("CP-R5-CU08 - Trazabilidad y comunicación por ocupación confirmada")
    void debeRegistrarTrazabilidadYComunicacionPorOcupacion() {

        String codigoCaso = "CP-R5-CU08";
        String accion = "Ejecutar el control hasta producir el cambio de estado a OCUPADO.";
        String resultadoEsperado = "Resultado esperado: El módulo registra el estado OCUPADO, " +
                "envía y registra la comunicación OCUPACION_CONFIRMADA y conserva ambos eventos " +
                "para trazabilidad.\n\n" +
                "Objetivo: Verificar la trazabilidad y comunicación asociadas a una ocupación confirmada.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Trazabilidad y comunicación por ocupación confirmada"
        );

        try {

            EscenarioR5 escenario =
                    crearEscenario(codigoCaso);

            registrarTresValidaciones(escenario);

            precondiciones =
                    """
                    Reserva: %s
                    Estado inicial: PENDIENTE
                    Cantidad mínima requerida alcanzada
                    Servicio de correo electrónico habilitado
                    """.formatted(
                            escenario.reserva().getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.inicio().plusMinutes(5)
                    );

            List<CorreoElectronico> correos =
                    correoElectronicoRepository
                            .findByReservaOrderByFechaEnvioAsc(escenario.reserva());

            assertEquals(
                    1,
                    correos.size(),
                    "La cantidad de comunicaciones registradas no corresponde."
            );

            assertEquals(
                    TipoCorreo.OCUPACION_CONFIRMADA,
                    correos.get(0).getTipoCorreo(),
                    "El tipo de correo registrado no corresponde."
            );

            assertNotNull(
                    correos.get(0).getFechaEnvio(),
                    "La comunicación no posee fecha de envío."
            );

            TrazabilidadReservaResponse trazabilidad =
                    trazabilidadReservaService
                            .obtenerTrazabilidad(
                                    escenario.reserva().getIdReserva()
                            );

            assertEquals(
                    "OCUPADO",
                    trazabilidad.estadoActual(),
                    "La trazabilidad no muestra OCUPADO como estado actual."
            );

            assertTrue(
                    trazabilidad
                            .eventos()
                            .stream()
                            .anyMatch(evento ->
                                    evento.tipoEvento().equals("ESTADO_OCUPACION")
                                            && evento.detalle().startsWith("OCUPADO")
                            ),
                    "No se encontró el evento de ocupación."
            );

            assertTrue(
                    trazabilidad
                            .eventos()
                            .stream()
                            .anyMatch(evento ->
                                    evento.tipoEvento().equals("CORREO_ELECTRONICO")
                                            && evento.detalle().equals("OCUPACION_CONFIRMADA")
                            ),
                    "No se encontró el evento de comunicación."
            );

            String resultadoReal =
                    """
                    Estado registrado: OCUPADO
                    Correo enviado y comunicación registrada: OCUPACION_CONFIRMADA
                    Fecha de comunicación: %s
                    Los eventos fueron recuperados mediante la consulta de trazabilidad.
                    """.formatted(
                            correos.get(0).getFechaEnvio()
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU09
    @Test
    @DisplayName("CP-R5-CU09 - Trazabilidad y comunicación por liberación automática")
    void debeRegistrarTrazabilidadYComunicacionPorLiberacion() {

        String codigoCaso = "CP-R5-CU09";
        String accion = "Ejecutar el control al finalizar la tolerancia sin alcanzar la cantidad mínima.";
        String resultadoEsperado = "Resultado esperado: El módulo registra el estado LIBERADO, " +
                "envía y registra la comunicación LIBERACION_AUTOMATICA y conserva ambos eventos " +
                "para trazabilidad.\n\n" +
                "Objetivo: Verificar la trazabilidad y comunicación asociadas a la liberación automática.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Trazabilidad y comunicación por liberación automática"
        );

        try {

            EscenarioR5 escenario =
                    crearEscenario(codigoCaso);

            registrarValidacion(
                    escenario.integrantes().get(0),
                    escenario.inicio().plusMinutes(1)
            );

            registrarValidacion(
                    escenario.integrantes().get(1),
                    escenario.inicio().plusMinutes(2)
            );

            precondiciones =
                    """
                    Reserva: %s
                    Estado inicial: PENDIENTE
                    Cantidad mínima requerida no alcanzada
                    Periodo de tolerancia finalizado
                    Servicio de correo electrónico habilitado
                    """.formatted(
                            escenario.reserva().getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.limite()
                    );

            List<CorreoElectronico> correos =
                    correoElectronicoRepository
                            .findByReservaOrderByFechaEnvioAsc(escenario.reserva());

            assertEquals(
                    1,
                    correos.size(),
                    "La cantidad de comunicaciones registradas no corresponde."
            );

            assertEquals(
                    TipoCorreo.LIBERACION_AUTOMATICA,
                    correos.get(0).getTipoCorreo(),
                    "El tipo de correo registrado no corresponde."
            );

            assertNotNull(
                    correos.get(0).getFechaEnvio(),
                    "La comunicación no posee fecha de envío."
            );

            TrazabilidadReservaResponse trazabilidad =
                    trazabilidadReservaService
                            .obtenerTrazabilidad(
                                    escenario.reserva().getIdReserva()
                            );

            assertEquals(
                    "LIBERADO",
                    trazabilidad.estadoActual(),
                    "La trazabilidad no muestra LIBERADO como estado actual."
            );

            assertTrue(
                    trazabilidad
                            .eventos()
                            .stream()
                            .anyMatch(evento ->
                                    evento.tipoEvento().equals("ESTADO_OCUPACION")
                                            && evento.detalle().startsWith("LIBERADO")
                            ),
                    "No se encontró el evento de liberación."
            );

            assertTrue(
                    trazabilidad
                            .eventos()
                            .stream()
                            .anyMatch(evento ->
                                    evento.tipoEvento().equals("CORREO_ELECTRONICO")
                                            && evento.detalle().equals("LIBERACION_AUTOMATICA")
                            ),
                    "No se encontró el evento de comunicación."
            );

            String resultadoReal =
                    """
                    Estado registrado: LIBERADO
                    Correo enviado y comunicación registrada: LIBERACION_AUTOMATICA
                    Fecha de comunicación: %s
                    Los eventos fueron recuperados mediante la consulta de trazabilidad.
                    """.formatted(
                            correos.get(0).getFechaEnvio()
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU10
    @Test
    @DisplayName("CP-R5-CU10 - Consulta del estado de ocupación")
    void debeConsultarEstadoActualDeOcupacion() {

        String codigoCaso = "CP-R5-CU10";
        String accion = "Consultar el estado de ocupación actual asociado a la reserva.";
        String resultadoEsperado = "Resultado esperado: El módulo muestra el estado de ocupación actual " +
                "sin modificar la información almacenada.\n\n" +
                "Objetivo: Verificar la consulta administrativa del estado de ocupación de una reserva.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Consulta del estado de ocupación"
        );

        try {

            EscenarioR5 escenario =
                    crearEscenario(codigoCaso);

            registrarTresValidaciones(escenario);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.inicio().plusMinutes(5)
                    );

            int estadosAntes =
                    obtenerEstadosAutomaticos(escenario.reserva()).size();

            precondiciones =
                    """
                    Reserva: %s
                    Existe información de ocupación registrada
                    Estado actual esperado: OCUPADO
                    """.formatted(
                            escenario.reserva().getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            ControlOcupacionResponse consulta =
                    controlOcupacionConsultaService
                            .obtenerControl(
                                    escenario.reserva().getIdReserva()
                            );

            int estadosDespues =
                    obtenerEstadosAutomaticos(escenario.reserva()).size();

            assertEquals(
                    "OCUPADO",
                    consulta.estadoActual(),
                    "La consulta no devolvió el estado esperado."
            );

            assertEquals(
                    escenario.reserva().getIdReserva(),
                    consulta.idReserva(),
                    "El identificador de reserva no corresponde."
            );

            assertEquals(
                    escenario.reserva().getCodigoReserva(),
                    consulta.codigoReserva(),
                    "El código de reserva no corresponde."
            );

            assertEquals(
                    estadosAntes,
                    estadosDespues,
                    "La consulta modificó el historial de estados."
            );

            String resultadoReal =
                    """
                    Reserva consultada: %s
                    Estado mostrado: %s
                    Registros antes de consultar: %d
                    Registros después de consultar: %d
                    La consulta no modificó la información almacenada.
                    """.formatted(
                            consulta.codigoReserva(),
                            consulta.estadoActual(),
                            estadosAntes,
                            estadosDespues
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    // CP-R5-CU11
    @Test
    @DisplayName("CP-R5-CU11 - Consulta del historial de eventos")
    void debeConsultarHistorialDeEventosEnOrdenCronologico() {

        String codigoCaso = "CP-R5-CU11";
        String accion = "Consultar el historial completo de eventos asociados a la reserva.";
        String resultadoEsperado = "Resultado esperado: El módulo muestra los eventos registrados " +
                "en orden cronológico, indicando su tipo, fecha, hora e información asociada.\n\n" +
                "Objetivo: Verificar la trazabilidad completa de los eventos generados para una reserva.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(
                codigoCaso,
                "Consulta del historial de eventos"
        );

        try {

            EscenarioR5 escenario =
                    crearEscenario(codigoCaso);

            registrarTresValidaciones(escenario);

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            escenario.reserva(),
                            escenario.inicio().plusMinutes(5)
                    );

            precondiciones =
                    """
                    Reserva: %s
                    Existen validaciones de ingreso registradas
                    Existe historial de estados de ocupación
                    Existe una comunicación registrada
                    """.formatted(
                            escenario.reserva().getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            TrazabilidadReservaResponse trazabilidad =
                    trazabilidadReservaService
                            .obtenerTrazabilidad(
                                    escenario.reserva().getIdReserva()
                            );

            assertEquals(
                    "OCUPADO",
                    trazabilidad.estadoActual(),
                    "El estado actual de la trazabilidad no corresponde."
            );

            assertEquals(
                    escenario.limite(),
                    trazabilidad.fechaHoraLimiteTolerancia(),
                    "La fecha límite de tolerancia no corresponde."
            );

            List<EventoTrazabilidadResponse> eventos =
                    trazabilidad.eventos();

            assertTrue(
                    eventos.size() >= 6,
                    "No se recuperaron todos los eventos esperados."
            );

            assertTrue(
                    eventos
                            .stream()
                            .anyMatch(evento ->
                                    evento.tipoEvento().equals("VALIDACION_INGRESO")
                            ),
                    "No se encontraron eventos de validación."
            );

            assertTrue(
                    eventos
                            .stream()
                            .anyMatch(evento ->
                                    evento.tipoEvento().equals("ESTADO_OCUPACION")
                            ),
                    "No se encontraron eventos de ocupación."
            );

            assertTrue(
                    eventos
                            .stream()
                            .anyMatch(evento ->
                                    evento.tipoEvento().equals("CORREO_ELECTRONICO")
                            ),
                    "No se encontraron eventos de comunicación."
            );

            for (int i = 1;i < eventos.size();i++) {

                assertFalse(
                        eventos
                                .get(i)
                                .fechaHora()
                                .isBefore(
                                        eventos
                                                .get(i - 1)
                                                .fechaHora()
                                ),
                        "Los eventos no se encuentran en orden cronológico."
                );
            }

            String resultadoReal =
                    """
                    Reserva consultada: %s
                    Estado actual: %s
                    Cantidad de eventos recuperados: %d
                    Se encontraron eventos de validación, ocupación y comunicación.
                    Todos los eventos se encuentran ordenados cronológicamente.
                    """.formatted(
                            trazabilidad.codigoReserva(),
                            trazabilidad.estadoActual(),
                            eventos.size()
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"APROBADA");

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal =
                    "Prueba no superada: " + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(codigoCaso,resultadoReal,"NO_APROBADA");

            throw e;
        }
    }


    private EscenarioR5 crearEscenario(
            String codigoCaso) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        EscenarioR5 escenario =
                transactionTemplate.execute(
                        status -> {

                            Ambiente ambiente =
                                    ambienteRepository
                                            .findByCodigo(CODIGO_AMBIENTE)
                                            .orElseThrow(() ->
                                                    new IllegalStateException(
                                                            "No existe el ambiente "
                                                                    + CODIGO_AMBIENTE + "."
                                                    )
                                            );

                            ambiente.getUbicacion().getNombre();

                            assertEquals(
                                    3,
                                    ambiente.getCantidadMinima(),
                                    "El ambiente utilizado debe requerir 3 integrantes."
                            );

                            Usuario administrador =
                                    usuarioRepository
                                            .findByDni("00000001")
                                            .orElseThrow(() ->
                                                    new IllegalStateException(
                                                            "No existe el usuario administrador base."
                                                    )
                                            );

                            LocalDateTime fechaCreacion =
                                    LocalDateTime.now();

                            LocalDateTime inicio =
                                    fechaCreacion
                                            .plusDays(1)
                                            .withMinute(0)
                                            .withSecond(0)
                                            .withNano(0);

                            Reserva reserva = new Reserva();
                            reserva.setCodigoReserva(
                                    generarCodigoReserva(codigoCaso)
                            );
                            reserva.setAmbiente(ambiente);
                            reserva.setFechaHoraInicio(inicio);
                            reserva.setFechaHoraFin(inicio.plusHours(1));
                            reserva.setToleranciaMinutos(
                                    ReglasControlOcupacion.MINUTOS_TOLERANCIA
                            );
                            reserva.setFechaCreacion(fechaCreacion);
                            reserva.setUsuarioCreacion(administrador);

                            reserva =
                                    reservaRepository.save(reserva);

                            List<ReservaUsuario> integrantes =
                                    new ArrayList<>();

                            for (int i = 0;i < DNIS_INTEGRANTES.size();i++) {

                                Usuario usuario =
                                        usuarioRepository
                                                .findByDni(DNIS_INTEGRANTES.get(i))
                                                .orElseThrow(() ->
                                                        new IllegalStateException(
                                                                "No existe uno de los usuarios base."
                                                        )
                                                );

                                ReservaUsuario reservaUsuario =
                                        new ReservaUsuario();

                                reservaUsuario.setReserva(reserva);
                                reservaUsuario.setUsuario(usuario);
                                reservaUsuario.setRolEnReserva(
                                        i == 0
                                                ? RolEnReserva.RESPONSABLE
                                                : RolEnReserva.INTEGRANTE
                                );
                                reservaUsuario.setActivo(true);
                                reservaUsuario.setFechaCreacion(fechaCreacion);
                                reservaUsuario.setUsuarioCreacion(administrador);

                                integrantes.add(
                                        reservaUsuarioRepository
                                                .save(reservaUsuario)
                                );
                            }

                            ReservaEstado estado =
                                    new ReservaEstado();

                            estado.setReserva(reserva);
                            estado.setEstadoReserva(EstadoReserva.VIGENTE);
                            estado.setMotivo(
                                    "Estado vigente para prueba formal R5"
                            );
                            estado.setFechaHoraEstado(fechaCreacion);
                            estado.setFechaCreacion(fechaCreacion);
                            estado.setUsuarioCreacion(administrador);

                            reservaEstadoRepository.save(estado);

                            return new EscenarioR5(
                                    reserva,
                                    List.copyOf(integrantes),
                                    inicio,
                                    inicio.plusMinutes(
                                            ReglasControlOcupacion.MINUTOS_TOLERANCIA
                                    )
                            );
                        }
                );

        EscenarioR5 resultado =
                Objects.requireNonNull(escenario);

        reservasCreadas.add(
                resultado.reserva().getIdReserva()
        );

        return resultado;
    }


    private ReservaUsuario crearAsociacionDuplicada(
            Reserva reserva,
            Usuario usuario) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        return Objects.requireNonNull(
                transactionTemplate.execute(
                        status -> {

                            ReservaUsuario asociacion =
                                    new ReservaUsuario();

                            asociacion.setReserva(reserva);
                            asociacion.setUsuario(usuario);
                            asociacion.setRolEnReserva(RolEnReserva.INTEGRANTE);
                            asociacion.setActivo(true);
                            asociacion.setFechaCreacion(LocalDateTime.now());
                            asociacion.setUsuarioCreacion(null);

                            return reservaUsuarioRepository
                                    .save(asociacion);
                        }
                )
        );
    }


    private ValidacionIngreso registrarValidacion(
            ReservaUsuario reservaUsuario,
            LocalDateTime momento) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        return Objects.requireNonNull(
                transactionTemplate.execute(
                        status ->
                                guardarValidacion(
                                        reservaUsuario,
                                        momento
                                )
                )
        );
    }


    private ValidacionIngreso registrarValidacionConEvento(
            ReservaUsuario reservaUsuario,
            LocalDateTime momento) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        return Objects.requireNonNull(
                transactionTemplate.execute(
                        status -> {

                            ValidacionIngreso validacion =
                                    guardarValidacion(
                                            reservaUsuario,
                                            momento
                                    );

                            applicationEventPublisher.publishEvent(
                                    new ValidacionIngresoRegistradaEvent(
                                            reservaUsuario
                                                    .getReserva()
                                                    .getIdReserva(),
                                            momento
                                    )
                            );

                            return validacion;
                        }
                )
        );
    }


    private ValidacionIngreso guardarValidacion(
            ReservaUsuario reservaUsuario,
            LocalDateTime momento) {

        PuntoValidacion puntoValidacion =
                puntoValidacionRepository
                        .findByAmbienteAndActivoTrue(
                                reservaUsuario
                                        .getReserva()
                                        .getAmbiente()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El ambiente no posee un punto de validación activo."
                                )
                        );

        ValidacionIngreso validacion =
                new ValidacionIngreso();

        validacion.setReservaUsuario(reservaUsuario);
        validacion.setPuntoValidacion(puntoValidacion);
        validacion.setMedioValidacion(MedioValidacion.ESCANEO_SIMULADO);
        validacion.setTipoIdentificador(TipoIdentificador.DNI);
        validacion.setFechaHoraValidacion(momento);
        validacion.setFechaCreacion(momento);
        validacion.setUsuarioCreacion(null);

        return validacionIngresoRepository.save(validacion);
    }


    private void registrarTresValidaciones(
            EscenarioR5 escenario) {

        for (int i = 0;i < 3;i++) {

            registrarValidacion(
                    escenario.integrantes().get(i),
                    escenario.inicio().plusMinutes(i + 1L)
            );
        }
    }


    private EstadoOcupacion esperarEstado(
            Reserva reserva,
            EstadoOcupacion estadoEsperado) {

        long momentoLimite =
                System.currentTimeMillis()
                        + TIEMPO_MAXIMO_ESPERA_MS;

        while (System.currentTimeMillis() < momentoLimite) {

            EstadoOcupacion estadoActual =
                    obtenerEstadoActual(reserva);

            if (estadoActual == estadoEsperado) {
                return estadoActual;
            }

            try {

                Thread.sleep(INTERVALO_CONSULTA_MS);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new AssertionError(
                        "La espera del estado fue interrumpida.",
                        e
                );
            }
        }

        throw new AssertionError(
                "No se alcanzó el estado "
                        + estadoEsperado
                        + " dentro del tiempo máximo de espera."
        );
    }


    private EstadoOcupacion obtenerEstadoActual(
            Reserva reserva) {

        return controlOcupacionRepository
                .findByReserva(reserva)
                .flatMap(control ->
                        ocupacionEstadoRepository
                                .findTopByControlOcupacionAndFechaHoraInicioPeriodoAndFechaHoraFinPeriodoOrderByFechaHoraEstadoDesc(
                                        control,
                                        reserva.getFechaHoraInicio(),
                                        reserva.getFechaHoraFin()
                                )
                )
                .map(OcupacionEstado::getEstadoOcupacion)
                .orElse(null);
    }


    private List<OcupacionEstado> obtenerEstadosAutomaticos(
            Reserva reserva) {

        ControlOcupacion controlOcupacion =
                controlOcupacionRepository
                        .findByReserva(reserva)
                        .orElseThrow(() ->
                                new AssertionError(
                                        "No se creó el control de ocupación esperado."
                                )
                        );

        return ocupacionEstadoRepository
                .findByControlOcupacionOrderByFechaHoraEstadoAsc(controlOcupacion)
                .stream()
                .filter(estado ->
                        reserva.getFechaHoraInicio().equals(
                                estado.getFechaHoraInicioPeriodo()
                        )
                                && reserva.getFechaHoraFin().equals(
                                estado.getFechaHoraFinPeriodo()
                        )
                )
                .toList();
    }


    private String generarCodigoReserva(
            String codigoCaso) {

        long sufijo =
                Math.floorMod(
                        System.nanoTime(),
                        1_000_000L
                );

        return "R5-"
                + codigoCaso.replace("CP-R5-","")
                + "-"
                + "%06d".formatted(sufijo);
    }


    private void guardarEvidencia(
            String codigoCaso,
            String precondiciones,
            String accion,
            String resultadoEsperado,
            String resultadoReal,
            String estado) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        transactionTemplate.executeWithoutResult(
                status -> {

                    EvidenciaPrueba evidencia =
                            new EvidenciaPrueba();

                    evidencia.setCodigoCaso(codigoCaso);
                    evidencia.setFechaHoraEjecucion(LocalDateTime.now());
                    evidencia.setPrecondiciones(precondiciones);
                    evidencia.setAccion(accion);
                    evidencia.setResultadoEsperado(resultadoEsperado);
                    evidencia.setResultadoReal(resultadoReal);
                    evidencia.setEstado(estado);

                    entityManager.persist(evidencia);
                    entityManager.flush();
                }
        );
    }


    private void imprimirCabecera(
            String codigoCaso,
            String nombreCaso) {

        System.out.println(
                """
                ========================================
                %s - %s
                ========================================
                """.formatted(
                        codigoCaso,
                        nombreCaso
                )
        );
    }


    private void imprimirPrecondiciones(
            String precondiciones) {

        System.out.println(
                "Precondiciones:\n"
                        + precondiciones
                        + "\n"
        );
    }


    private void imprimirInstrucciones(
            String accion,
            String resultadoEsperado) {

        System.out.println(
                """
                Acción: %s
                
                Esperado: %s
                """.formatted(
                        accion,
                        resultadoEsperado
                )
        );
    }


    private void imprimirResultado(
            String codigoCaso,
            String resultadoReal,
            String estado) {

        System.out.println(
                """
                ========================================
                Resultado real: %s
                
                %s - %s
                ========================================
                """.formatted(
                        resultadoReal,
                        codigoCaso,
                        estado
                )
        );
    }


    private String obtenerMensajeError(
            Throwable error) {

        if (error.getMessage() == null
                || error.getMessage().isBlank()) {

            return error
                    .getClass()
                    .getSimpleName();
        }

        return error.getMessage();
    }


    private record EscenarioR5(
            Reserva reserva,
            List<ReservaUsuario> integrantes,
            LocalDateTime inicio,
            LocalDateTime limite) {
    }
}