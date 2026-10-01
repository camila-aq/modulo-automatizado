package com.camila.moduloautomatizado.service.functional;

import com.camila.moduloautomatizado.model.entity.EvidenciaPrueba;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;

import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class ValidacionIngresoManualCasoUsoTest {

    private static final String CODIGO_RESERVA = "RES-INT-CCSS-001";
    private static final String CODIGO_AMBIENTE = "CCSS-AMB-001";
    private static final String CODIGO_PUNTO = "PVAL-CCSS-001";
    private static final String NOMBRE_UBICACION = "COMPLEJO DE CIENCIAS SOCIALES";
    private static final long TIEMPO_MAXIMO_ESPERA_MS = 300_000;
    private static final long INTERVALO_CONSULTA_MS = 500;

    private static final String DNI_CODIGO_UNIVERSITARIO = "10000003";
    private static final String CODIGO_UNIVERSITARIO = "EST0000003";

    private static final String DNI = "10000002";

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaUsuarioRepository reservaUsuarioRepository;

    @Autowired
    private ReservaEstadoRepository reservaEstadoRepository;

    @Autowired
    private ValidacionIngresoRepository validacionIngresoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;


    // CP-R3-CU07
    @Test
    @DisplayName("CP-R3-CU07 - Validación manual aceptada mediante DNI")
    void debeAceptarValidacionManualMedianteDni()
            throws InterruptedException {

        String codigoCaso = "CP-R3-CU07";
        String accion = "Ingreso del DNI " + DNI
                + ", localización de la reserva y confirmación manual del ingreso.";
        String resultadoEsperado = "Resultado esperado: El módulo acepta la validación, registra la " +
                "fecha y hora de ingreso y muestra el resultado correspondiente.\n\n" +
                "Objetivo: Verificar el flujo básico de validación manual cuando se utiliza " +
                "el DNI del usuario.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(codigoCaso,"Validación manual aceptada mediante DNI");

        try {

            var usuario =
                    usuarioRepository
                            .findByDni(DNI)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe el usuario con DNI " + DNI + "."
                                    )
                            );

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

            assertEquals(
                    NOMBRE_UBICACION,
                    reserva.getAmbiente().getUbicacion().getNombre(),
                    "La reserva no corresponde a la ubicación esperada."
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

            precondiciones =
                    """
                    Estudiante: %s %s
                    DNI: %s
                    Ubicación: %s
                    Ambiente: %s
                    Reserva vigente: %s
                    No existe validación aceptada previa
                    """.formatted(
                            usuario.getNombres(),
                            usuario.getApellidos(),
                            usuario.getDni(),
                            reserva.getAmbiente().getUbicacion().getNombre(),
                            reserva.getAmbiente().getCodigo(),
                            reserva.getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            ValidacionIngreso validacion =
                    esperarValidacion(reservaUsuario);

            entityManager.refresh(validacion);

            assertAll(
                    "Resultado de CP-R3-CU07",
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
                                    validacion
                                            .getReservaUsuario()
                                            .getReserva()
                                            .getAmbiente()
                                            .getCodigo(),
                                    "El ambiente registrado no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    NOMBRE_UBICACION,
                                    validacion
                                            .getReservaUsuario()
                                            .getReserva()
                                            .getAmbiente()
                                            .getUbicacion()
                                            .getNombre(),
                                    "La ubicación registrada no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_PUNTO,
                                    validacion.getPuntoValidacion().getCodigoPunto(),
                                    "El punto de validación no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    MedioValidacion.INGRESO_MANUAL,
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
                    Ubicación: %s
                    Ambiente: %s
                    Punto: %s
                    Medio: %s
                    Tipo de identificador: %s
                    Fecha y hora: %s
                    """.formatted(
                            validacion.getReservaUsuario().getReserva().getCodigoReserva(),
                            validacion.getReservaUsuario().getReserva().getAmbiente().getUbicacion().getNombre(),
                            validacion.getReservaUsuario().getReserva().getAmbiente().getCodigo(),
                            validacion.getPuntoValidacion().getCodigoPunto(),
                            validacion.getMedioValidacion(),
                            validacion.getTipoIdentificador(),
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

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            String resultadoReal =
                    "Prueba interrumpida antes de completar la validación.";

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

    // CP-R3-CU08
    @Test
    @DisplayName("CP-R3-CU08 - Validación manual aceptada mediante código universitario")
    void debeAceptarValidacionManualMedianteCodigoUniversitario()
            throws InterruptedException {

        String codigoCaso = "CP-R3-CU08";
        String accion = "Ingreso del código universitario " + CODIGO_UNIVERSITARIO
                + ", localización de la reserva y confirmación manual del ingreso.";
        String resultadoEsperado = "Resultado esperado: El módulo acepta la validación, registra la " +
                "fecha y hora de ingreso y muestra el resultado correspondiente.\n\n" +
                "Objetivo: Verificar el flujo básico de validación manual utilizando " +
                "el código universitario.";
        String precondiciones = "No fue posible verificar las precondiciones.";

        imprimirCabecera(codigoCaso,"Validación manual aceptada mediante código universitario");

        try {

            var usuario =
                    usuarioRepository
                            .findByCodigoUniversitario(CODIGO_UNIVERSITARIO)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe el usuario con código universitario "
                                                    + CODIGO_UNIVERSITARIO + "."
                                    )
                            );

            assertEquals(
                    DNI_CODIGO_UNIVERSITARIO,
                    usuario.getDni(),
                    "El código universitario no corresponde al estudiante esperado."
            );

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

            assertEquals(
                    NOMBRE_UBICACION,
                    reserva.getAmbiente().getUbicacion().getNombre(),
                    "La reserva no corresponde a la ubicación esperada."
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

            precondiciones =
                    """
                    Estudiante: %s %s
                    Código universitario: %s
                    Ubicación: %s
                    Ambiente: %s
                    Reserva vigente: %s
                    No existe validación aceptada previa
                    """.formatted(
                            usuario.getNombres(),
                            usuario.getApellidos(),
                            usuario.getCodigoUniversitario(),
                            reserva.getAmbiente().getUbicacion().getNombre(),
                            reserva.getAmbiente().getCodigo(),
                            reserva.getCodigoReserva()
                    ).trim();

            imprimirPrecondiciones(precondiciones);
            imprimirInstrucciones(accion,resultadoEsperado);

            ValidacionIngreso validacion =
                    esperarValidacion(reservaUsuario);

            entityManager.refresh(validacion);

            assertAll(
                    "Resultado de CP-R3-CU08",
                    () ->
                            assertNotNull(
                                    validacion.getIdValidacion(),
                                    "No se generó ID de validación."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_UNIVERSITARIO,
                                    validacion
                                            .getReservaUsuario()
                                            .getUsuario()
                                            .getCodigoUniversitario(),
                                    "El código universitario registrado no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_RESERVA,
                                    validacion
                                            .getReservaUsuario()
                                            .getReserva()
                                            .getCodigoReserva(),
                                    "La reserva registrada no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_AMBIENTE,
                                    validacion
                                            .getReservaUsuario()
                                            .getReserva()
                                            .getAmbiente()
                                            .getCodigo(),
                                    "El ambiente registrado no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    NOMBRE_UBICACION,
                                    validacion
                                            .getReservaUsuario()
                                            .getReserva()
                                            .getAmbiente()
                                            .getUbicacion()
                                            .getNombre(),
                                    "La ubicación registrada no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    CODIGO_PUNTO,
                                    validacion
                                            .getPuntoValidacion()
                                            .getCodigoPunto(),
                                    "El punto de validación no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    MedioValidacion.INGRESO_MANUAL,
                                    validacion.getMedioValidacion(),
                                    "El medio de validación no corresponde."
                            ),
                    () ->
                            assertEquals(
                                    TipoIdentificador.CODIGO_UNIVERSITARIO,
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
                    Ubicación: %s
                    Ambiente: %s
                    Punto: %s
                    Medio: %s
                    Tipo de identificador: %s
                    Fecha y hora: %s
                    """.formatted(
                            validacion.getReservaUsuario().getReserva().getCodigoReserva(),
                            validacion.getReservaUsuario().getReserva().getAmbiente().getUbicacion().getNombre(),
                            validacion.getReservaUsuario().getReserva().getAmbiente().getCodigo(),
                            validacion.getPuntoValidacion().getCodigoPunto(),
                            validacion.getMedioValidacion(),
                            validacion.getTipoIdentificador(),
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

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            String resultadoReal =
                    "Prueba interrumpida antes de completar la validación.";

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

    private ValidacionIngreso esperarValidacion(
            ReservaUsuario reservaUsuario)
            throws InterruptedException {

        long momentoLimite =
                System.currentTimeMillis() + TIEMPO_MAXIMO_ESPERA_MS;

        while ( System.currentTimeMillis() < momentoLimite ) {

            entityManager.clear();

            ValidacionIngreso validacion =
                    buscarValidacionRegistrada(
                            reservaUsuario.getIdReservaUsuario()
                    );

            if (validacion != null) {
                return validacion;
            }

            Thread.sleep(INTERVALO_CONSULTA_MS);
        }

        throw new AssertionError(
                "No se detectó la validación esperada en la base de datos."
        );
    }

    private ValidacionIngreso buscarValidacionRegistrada(
            Integer idReservaUsuario) {

        return validacionIngresoRepository
                .findAll()
                .stream()
                .filter(validacion ->
                        Objects.equals(
                                validacion
                                        .getReservaUsuario()
                                        .getIdReservaUsuario(),
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
                """.formatted(codigoCaso,nombreCaso)
        );
    }

    private void imprimirPrecondiciones(
            String precondiciones) {

        System.out.println(
                "Precondiciones:\n" + precondiciones + "\n"
        );
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
                """.formatted(
                        resultadoReal,
                        codigoCaso,
                        estado
                )
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