package com.camila.moduloautomatizado.service.functional;

import com.camila.moduloautomatizado.model.entity.EvidenciaPrueba;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;

import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;
import com.camila.moduloautomatizado.service.ValidacionIngresoService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class ValidacionIngresoEscaneoCasoUsoTest {

    private static final String CODIGO_RESERVA = "RES-INT-CCSS-001";
    private static final String CODIGO_AMBIENTE = "CCSS-AMB-001";
    private static final String CODIGO_PUNTO = "PVAL-CCSS-001";
    private static final String DNI = "10000001";
    private static final long TIEMPO_MAXIMO_ESPERA_MS = 300_000;
    private static final long INTERVALO_CONSULTA_MS = 500;

    private static final String DNI_NO_REGISTRADO = "99999999";

    private static final String DNI_SIN_RESERVA = "10000028";

    private static final String DNI_RESERVA_FUERA_VIGENCIA = "10000007";
    private static final String CODIGO_RESERVA_FUERA_VIGENCIA = "RES-INT-CCSS-EXP-001";
    private static final String CODIGO_AMBIENTE_FUERA_VIGENCIA = "CCSS-AMB-002";
    private static final String CODIGO_PUNTO_FUERA_VIGENCIA = "PVAL-CCSS-002";

    private static final String DNI_OTRO_AMBIENTE = "10000004";
    private static final String CODIGO_RESERVA_OTRO_AMBIENTE = "RES-INT-CIA-001";
    private static final String CODIGO_AMBIENTE_OTRO_AMBIENTE = "CIA-AMB-001";

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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoSpyBean
    private ValidacionIngresoService validacionIngresoService;


    // CP-R3-CU01
    @Test
    @DisplayName("CP-R3-CU01 - Validación aceptada mediante escaneo simulado")
    void debeAceptarValidacionMedianteEscaneoSimulado()
            throws InterruptedException {

        String codigoCaso = "CP-R3-CU01";
        String accion =  "Escaneo del DNI " + DNI + " en el punto " + CODIGO_PUNTO + ".";
        String resultadoEsperado = "Resultado esperado: El módulo acepta la validación, registra la " +
                "fecha y hora de ingreso y muestra el resultado correspondiente.\n\n" +
                "Objetivo: Verificar el flujo básico del caso de uso cuando el estudiante cumple " +
                "las condiciones requeridas para validar su ingreso.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(codigoCaso,"Validación aceptada mediante escaneo simulado");

        try {
            //PRECONDICIONES

            Reserva reserva =
                    reservaRepository
                            .findByCodigoReserva(CODIGO_RESERVA)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe la reserva " + CODIGO_RESERVA + "."
                                    )
                            );

            assertEquals(
                    CODIGO_AMBIENTE,
                    reserva.getAmbiente().getCodigo(),
                    "La reserva no corresponde al ambiente esperado."
            );

            PuntoValidacion puntoValidacion =
                    puntoValidacionRepository
                            .findByCodigoPuntoAndActivoTrue(CODIGO_PUNTO)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe un punto activo " + "con código "
                                            + CODIGO_PUNTO + "."
                                    )
                            );

            assertEquals(
                    reserva.getAmbiente().getIdAmbiente(),
                    puntoValidacion.getAmbiente().getIdAmbiente(),
                    "El punto de validación no pertenece al ambiente de la reserva."
            );

            ReservaUsuario reservaUsuario =
                    reservaUsuarioRepository
                            .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(reserva)
                            .stream()
                            .filter(asociacion -> DNI.equals(asociacion.getUsuario().getDni()))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "El usuario con DNI " + DNI
                                            + " no se encuentra asociado a "
                                            + CODIGO_RESERVA + "."
                                    )
                            );

            ReservaEstado ultimoEstado =
                    reservaEstadoRepository
                            .findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "La reserva no tiene un estado registrado."
                                    )
                            );

            assertEquals(
                    EstadoReserva.VIGENTE,
                    ultimoEstado.getEstadoReserva(),
                    "La reserva no tiene estado VIGENTE."
            );

            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime inicioPermitido =
                    reserva
                            .getFechaHoraInicio()
                            .minusMinutes(ReglasControlOcupacion.MINUTOS_ANTICIPACION);

            assertTrue(
                    !ahora.isBefore(inicioPermitido) && ahora.isBefore(reserva.getFechaHoraFin()),
                    "La reserva no se encuentra dentro del periodo permitido."
            );

            assertFalse(
                    validacionIngresoRepository.existsByReservaUsuario(reservaUsuario),
                    "El usuario ya posee una validación para esta reserva."
            );

            precondiciones =
                    """
                    Estudiante: %s %s
                    Ambiente: %s
                    Reserva vigente
                    No existe validación aceptada previa
                    """.formatted(
                            reservaUsuario.getUsuario().getNombres(),
                            reservaUsuario.getUsuario().getApellidos(),
                            reserva.getAmbiente().getCodigo()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            ValidacionIngreso validacion = esperarValidacion(reservaUsuario);

            entityManager.refresh(validacion);

            assertAll(
                    "Resultado de CP-R3-CU01",
                    () ->
                            assertNotNull(
                                    validacion.getIdValidacion(),
                                    "No se generó ID de validación."
                            ),
                    () ->
                            assertEquals(
                                    DNI,
                                    validacion.getReservaUsuario().getUsuario().getDni(),
                                    "El DNI registrado no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_RESERVA,
                                    validacion.getReservaUsuario().getReserva().getCodigoReserva(),
                                    "La reserva registrada no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_AMBIENTE,
                                    validacion.getReservaUsuario().getReserva().getAmbiente().getCodigo(),
                                    "El ambiente registrado no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_PUNTO,
                                    validacion.getPuntoValidacion().getCodigoPunto(),
                                    "El punto de validación no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    MedioValidacion.ESCANEO_SIMULADO,
                                    validacion.getMedioValidacion(),
                                    "El medio de validación no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    TipoIdentificador.DNI,
                                    validacion.getTipoIdentificador(),
                                    "El tipo de identificador no corresponde."
                            ),
                    () ->
                            assertNotNull(
                                    validacion.getFechaHoraValidacion(),
                                    "No se registró fecha y hora."
                            )
            );

            String resultadoReal =
                    """
                    Validación registrada.
                    Reserva: %s
                    Punto: %s
                    Medio: %s
                    Fecha y hora: %s
                    """.formatted(
                            validacion.getReservaUsuario().getReserva().getCodigoReserva(),
                            validacion.getPuntoValidacion().getCodigoPunto(),
                            validacion .getMedioValidacion(),
                            validacion.getFechaHoraValidacion()
                    ).trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(
                    codigoCaso,
                    resultadoReal,
                    "APROBADA"
            );

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

            imprimirResultado(
                    codigoCaso,
                    resultadoReal,
                    "NO_APROBADA"
            );

            throw e;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            String resultadoReal = "Prueba interrumpida antes de completar la validación.";

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(
                    codigoCaso,
                    resultadoReal,
                    "NO_APROBADA"
            );

            throw e;
        }
    }

    // CP-R3-CU02
    @Test
    @DisplayName("CP-R3-CU02 - DNI no registrado")
    void debeRechazarDniNoRegistrado() {

        String codigoCaso = "CP-R3-CU02";
        String accion = "Escaneo del DNI " + DNI_NO_REGISTRADO + " en el punto " + CODIGO_PUNTO + ".";
        String resultadoEsperado = "Resultado esperado: El módulo rechaza la validación, " +
                "no registra el ingreso y muestra el resultado correspondiente.\n\n" +
                "Objetivo: Verificar el flujo alternativo cuando el DNI obtenido " +
                "no corresponde a un usuario registrado.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(codigoCaso,"DNI no registrado");

        try {

            assertTrue(
                    usuarioRepository
                            .findByDni(DNI_NO_REGISTRADO).isEmpty(),
                    "El DNI utilizado para CP-R3-CU02 se encuentra registrado."
            );

            PuntoValidacion puntoValidacion =
                    puntoValidacionRepository
                            .findByCodigoPuntoAndActivoTrue(CODIGO_PUNTO)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe un punto activo con código "
                                            + CODIGO_PUNTO + "."
                                    )
                            );

            precondiciones =
                    """
                    DNI: %s no registrado
                    Ambiente: %s
                    Punto de validación activo
                    """.formatted(
                            DNI_NO_REGISTRADO,
                            puntoValidacion.getAmbiente().getCodigo()
                    ).trim();

            imprimirPrecondiciones(precondiciones);

            long validacionesAntes = validacionIngresoRepository.count();

            imprimirInstrucciones(accion,resultadoEsperado);

            esperarAccionEscaneo(DNI_NO_REGISTRADO,CODIGO_PUNTO);

            entityManager.clear();

            long validacionesDespues = validacionIngresoRepository.count();

            assertEquals(
                    validacionesAntes,
                    validacionesDespues,
                    "Se registró una nueva validación de ingreso para un DNI no registrado."
            );

            String resultadoReal =
                    """
                    Validación rechazada.
                    No se registró una nueva validación de ingreso.
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

    // CP-R3-CU03
    @Test
    @DisplayName("CP-R3-CU03 - Usuario sin reserva asociada")
    void debeRechazarUsuarioSinReservaAsociada() {

        String codigoCaso = "CP-R3-CU03";
        String accion = "Escaneo del DNI " + DNI_SIN_RESERVA + " en el punto " + CODIGO_PUNTO + ".";
        String resultadoEsperado = "Resultado esperado: El módulo rechaza la validación " +
                "y no registra el ingreso.\n\n" +
                "Objetivo: Verificar el rechazo de la validación cuando el usuario " +
                "registrado no se encuentra asociado a una reserva.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(codigoCaso,"Usuario sin reserva asociada");

        try {

            var usuario =
                    usuarioRepository
                            .findByDni( DNI_SIN_RESERVA)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe el usuario con DNI " + DNI_SIN_RESERVA + "."
                                    )
                            );

            assertTrue(
                    reservaUsuarioRepository
                            .findByUsuarioAndActivoTrue(usuario)
                            .isEmpty(),
                    "El usuario utilizado para CP-R3-CU03 sí posee una reserva asociada."
            );

            PuntoValidacion puntoValidacion =
                    puntoValidacionRepository
                            .findByCodigoPuntoAndActivoTrue(CODIGO_PUNTO)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe un punto activo con código " + CODIGO_PUNTO + "."
                                    )
                            );

            precondiciones =
                    """
                    Estudiante: %s %s
                    DNI: %s
                    No tiene reserva asociada
                    Punto: %s activo
                    """.formatted(
                            usuario.getNombres(),
                            usuario.getApellidos(),
                            usuario.getDni(),
                            puntoValidacion.getCodigoPunto()
                    ).trim();

            imprimirPrecondiciones(precondiciones);

            long validacionesAntes = validacionIngresoRepository.count();

            imprimirInstrucciones(accion,resultadoEsperado);

            esperarAccionEscaneo(DNI_SIN_RESERVA,CODIGO_PUNTO);

            entityManager.clear();

            long validacionesDespues = validacionIngresoRepository.count();

            assertEquals(
                    validacionesAntes,
                    validacionesDespues,
                    "Se registró una validación para un usuario sin reserva asociada."
            );

            String resultadoReal =
                    """
                    Validación rechazada.
                    No se registró una nueva validación de ingreso.
                    """.trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(
                    codigoCaso,
                    resultadoReal,
                    "APROBADA"
            );

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

            imprimirResultado(
                    codigoCaso,
                    resultadoReal,
                    "NO_APROBADA"
            );
            
            throw e;
        }
    }

    // CP-R3-CU04
    @Test
    @DisplayName("CP-R3-CU04 - Reserva fuera de su periodo de vigencia")
    void debeRechazarReservaFueraDeVigencia() {

        String codigoCaso = "CP-R3-CU04";
        String accion = "Escaneo del DNI " + DNI_RESERVA_FUERA_VIGENCIA
                + " en el punto " + CODIGO_PUNTO_FUERA_VIGENCIA + ".";
        String resultadoEsperado = "Resultado esperado: El módulo rechaza la " +
                "validación y no registra el ingreso.\n\n" +
                "Objetivo: Verificar el rechazo cuando la reserva asociada al " +
                "usuario no se encuentra vigente al momento de la validación.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(codigoCaso,"Reserva fuera de su periodo de vigencia");

        try {
            var usuario =
                    usuarioRepository
                            .findByDni(DNI_RESERVA_FUERA_VIGENCIA)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe el usuario con DNI "
                                            + DNI_RESERVA_FUERA_VIGENCIA + "."
                                    )
                            );

            Reserva reserva =
                    reservaRepository
                            .findByCodigoReserva(CODIGO_RESERVA_FUERA_VIGENCIA)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe la reserva "
                                            + CODIGO_RESERVA_FUERA_VIGENCIA + "."
                                    )
                            );

            assertEquals(
                    CODIGO_AMBIENTE_FUERA_VIGENCIA,
                    reserva.getAmbiente() .getCodigo(),
                    "La reserva no corresponde al ambiente esperado."
            );

            ReservaUsuario reservaUsuario =
                    reservaUsuarioRepository
                            .findByReservaAndUsuario(reserva,usuario)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "El estudiante no se encuentra asociado a la reserva."
                                    )
                            );

            assertTrue(
                    reservaUsuario.getActivo(),
                    "La asociación del estudiante con la reserva no está activa."
            );

            ReservaEstado ultimoEstado =
                    reservaEstadoRepository
                            .findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "La reserva no tiene un estado registrado."
                                    )
                            );

            assertEquals(
                    EstadoReserva.VIGENTE,
                    ultimoEstado.getEstadoReserva(),
                    "La reserva no tiene estado VIGENTE."
            );

            assertTrue(
                    LocalDateTime.now().isAfter(reserva.getFechaHoraFin()),
                    "La reserva todavía se encuentra dentro de su periodo de vigencia."
            );

            assertFalse(
                    validacionIngresoRepository
                            .existsByReservaUsuario(reservaUsuario),
                    "El estudiante ya posee una validación para esta reserva."
            );

            PuntoValidacion puntoValidacion =
                    puntoValidacionRepository
                            .findByCodigoPuntoAndActivoTrue(CODIGO_PUNTO_FUERA_VIGENCIA)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe el punto activo "
                                            + CODIGO_PUNTO_FUERA_VIGENCIA + "."
                                    )
                            );

            assertEquals(
                    reserva.getAmbiente().getIdAmbiente(),
                    puntoValidacion.getAmbiente().getIdAmbiente(),
                    "El punto no corresponde al ambiente reservado."
            );

            precondiciones =
                    """
                    Estudiante: %s %s
                    Ambiente: %s
                    Reserva: %s
                    Periodo de reserva finalizado
                    No existe validación aceptada previa
                    """.formatted(
                            usuario.getNombres(),
                            usuario.getApellidos(),
                            reserva.getAmbiente().getCodigo(),
                            reserva.getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);

            long validacionesAntes = validacionIngresoRepository.count();

            imprimirInstrucciones(accion,resultadoEsperado);

            esperarAccionEscaneo(DNI_RESERVA_FUERA_VIGENCIA,CODIGO_PUNTO_FUERA_VIGENCIA);

            entityManager.clear();

            long validacionesDespues = validacionIngresoRepository.count();

            assertEquals(
                    validacionesAntes,
                    validacionesDespues,
                    "Se registró una validación para una reserva fuera de vigencia."
            );

            String resultadoReal =
                    """
                    Validación rechazada.
                    No se registró una nueva validación de ingreso.
                    """.trim();

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "APROBADA"
            );

            imprimirResultado(
                    codigoCaso,
                    resultadoReal,
                    "APROBADA"
            );

        } catch (AssertionError | RuntimeException e) {

            String resultadoReal = "Prueba no superada: "  + obtenerMensajeError(e);

            guardarEvidencia(
                    codigoCaso,
                    precondiciones,
                    accion,
                    resultadoEsperado,
                    resultadoReal,
                    "NO_APROBADA"
            );

            imprimirResultado(
                    codigoCaso,
                    resultadoReal,
                    "NO_APROBADA"
            );

            throw e;
        }
    }

    // CP-R3-CU05
    @Test
    @DisplayName("CP-R3-CU05 - Reserva correspondiente a otro ambiente")
    void debeRechazarReservaCorrespondienteAOtroAmbiente() {

        String codigoCaso = "CP-R3-CU05";
        String accion = "Escaneo del DNI " + DNI_OTRO_AMBIENTE + " en el punto " + CODIGO_PUNTO + ".";
        String resultadoEsperado = "Resultado esperado: El módulo rechaza la validación " +
                "y no registra el ingreso.\n\n" +
                "Objetivo: Verificar el rechazo cuando la reserva vigente corresponde " +
                "a un ambiente distinto al asociado con el punto de validación.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(codigoCaso,"Reserva correspondiente a otro ambiente");

        try {

            var usuario =
                    usuarioRepository
                            .findByDni(DNI_OTRO_AMBIENTE)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe el usuario con DNI "
                                                    + DNI_OTRO_AMBIENTE + "."
                                    )
                            );

            Reserva reserva =
                    reservaRepository
                            .findByCodigoReserva(CODIGO_RESERVA_OTRO_AMBIENTE)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe la reserva "
                                                    + CODIGO_RESERVA_OTRO_AMBIENTE + "."
                                    )
                            );

            assertEquals(
                    CODIGO_AMBIENTE_OTRO_AMBIENTE,
                    reserva.getAmbiente().getCodigo(),
                    "La reserva no corresponde al ambiente esperado."
            );

            ReservaUsuario reservaUsuario =
                    reservaUsuarioRepository
                            .findByReservaAndUsuario(reserva,usuario)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "El estudiante no se encuentra asociado a la reserva."
                                    )
                            );

            assertTrue(
                    reservaUsuario.getActivo(),
                    "La asociación del estudiante con la reserva no está activa."
            );

            ReservaEstado ultimoEstado =
                    reservaEstadoRepository
                            .findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "La reserva no tiene un estado registrado."
                                    )
                            );

            assertEquals(
                    EstadoReserva.VIGENTE,
                    ultimoEstado.getEstadoReserva(),
                    "La reserva no tiene estado VIGENTE."
            );

            LocalDateTime ahora = LocalDateTime.now();

            LocalDateTime inicioPermitido =
                    reserva
                            .getFechaHoraInicio()
                            .minusMinutes(ReglasControlOcupacion.MINUTOS_ANTICIPACION);

            assertTrue(
                    !ahora.isBefore(inicioPermitido)
                            && ahora.isBefore(reserva.getFechaHoraFin()),
                    "La reserva no se encuentra dentro del periodo permitido."
            );

            assertFalse(
                    validacionIngresoRepository.existsByReservaUsuario(reservaUsuario),
                    "El estudiante ya posee una validación para esta reserva."
            );

            PuntoValidacion puntoValidacion =
                    puntoValidacionRepository
                            .findByCodigoPuntoAndActivoTrue(CODIGO_PUNTO)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe un punto activo con código "
                                                    + CODIGO_PUNTO + "."
                                    )
                            );

            assertFalse(
                    Objects.equals(
                            reserva.getAmbiente().getIdAmbiente(),
                            puntoValidacion.getAmbiente().getIdAmbiente()
                    ),
                    "El punto de validación pertenece al mismo ambiente de la reserva."
            );

            precondiciones =
                    """
                    Estudiante: %s %s
                    Reserva vigente: %s
                    Ambiente de la reserva: %s
                    Ambiente del punto de validación: %s
                    No existe validación aceptada previa
                    """.formatted(
                            usuario.getNombres(),
                            usuario.getApellidos(),
                            reserva.getCodigoReserva(),
                            reserva.getAmbiente().getCodigo(),
                            puntoValidacion.getAmbiente().getCodigo()
                    ).trim();

            imprimirPrecondiciones(precondiciones);

            long validacionesAntes = validacionIngresoRepository.count();

            imprimirInstrucciones(accion,resultadoEsperado);

            esperarAccionEscaneo(DNI_OTRO_AMBIENTE,CODIGO_PUNTO);

            entityManager.clear();

            long validacionesDespues = validacionIngresoRepository.count();

            assertEquals(
                    validacionesAntes,
                    validacionesDespues,
                    "Se registró una validación desde un punto correspondiente a otro ambiente."
            );

            String resultadoReal =
                    """
                    Validación rechazada.
                    No se registró una nueva validación de ingreso.
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

    private void esperarAccionEscaneo(
            String dni,
            String codigoPunto) {

        verify(validacionIngresoService,timeout(TIEMPO_MAXIMO_ESPERA_MS))
                .validarIngresoPorEscaneo(dni,codigoPunto);
    }

    private ValidacionIngreso esperarValidacion(
            ReservaUsuario reservaUsuario)
            throws InterruptedException {

        long momentoLimite = System.currentTimeMillis() + TIEMPO_MAXIMO_ESPERA_MS;

        while ( System.currentTimeMillis() < momentoLimite ) {

            entityManager.clear();

            ValidacionIngreso validacion =
                    buscarValidacionRegistrada(reservaUsuario.getIdReservaUsuario());

            if (validacion != null) {
                return validacion;
            }

            Thread.sleep(INTERVALO_CONSULTA_MS);
        }

        throw new AssertionError("No se detectó la validación esperada en la base de datos."
        );
    }

    private ValidacionIngreso buscarValidacionRegistrada(
            Integer idReservaUsuario) {

        return validacionIngresoRepository
                .findAll()
                .stream()
                .filter(validacion ->
                        Objects.equals(
                                validacion.getReservaUsuario().getIdReservaUsuario(),
                                idReservaUsuario
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private void guardarEvidencia(
            String codigoCaso,
            String precondiciones,
            String accion,
            String resultadoEsperado,
            String resultadoReal,
            String estado) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        transactionTemplate.executeWithoutResult(
                status -> {

                    EvidenciaPrueba evidencia = new EvidenciaPrueba();
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
                """.formatted(codigoCaso,nombreCaso)
        );
    }

    private void imprimirPrecondiciones(
            String precondiciones) {

        System.out.println("Precondiciones:\n" + precondiciones + "\n");
    }

    private void imprimirInstrucciones(
            String accion,
            String resultadoEsperado) {

        System.out.println(
                """
                Acción: %s
                
                Esperado: %s
                
                Esperando acción desde el front...
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
                """.formatted(resultadoReal,codigoCaso,estado)
        );
    }

    private String obtenerMensajeError(
            Throwable error) {

        if ( error.getMessage() == null || error.getMessage().isBlank() ) {
            return error.getClass().getSimpleName();
        }

        return error.getMessage();
    }
}