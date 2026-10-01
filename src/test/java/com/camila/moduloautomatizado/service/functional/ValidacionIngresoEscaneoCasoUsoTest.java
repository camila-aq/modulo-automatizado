package com.camila.moduloautomatizado.service.functional;

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

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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
    private EntityManager entityManager;


    // CP-R3-CU01
    @Test
    @DisplayName("CP-R3-CU01 - Validación aceptada mediante escaneo simulado")
    void debeAceptarValidacionMedianteEscaneoSimulado()
            throws InterruptedException {

        System.out.println(
                """
                ========================================
                CP-R3-CU01
                Validación aceptada mediante escaneo simulado
                ========================================
                """
        );

        Reserva reserva = reservaRepository
                .findByCodigoReserva(CODIGO_RESERVA)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe la reserva "
                                + CODIGO_RESERVA + "."
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
                                        "No existe un punto activo con código "
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
                        .filter(
                                asociacion -> DNI.equals(asociacion.getUsuario().getDni())
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El usuario con DNI "
                                        + DNI + " no se encuentra asociado a "
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
                "La reserva ya no se encuentra dentro " +
                        "del periodo permitido para ejecutar CP-R3-CU01."
        );

        assertFalse(
                validacionIngresoRepository.existsByReservaUsuario(reservaUsuario),
                "CP-R3-CU01 no puede comenzar porque el usuario ya posee " +
                        "una validación para esta reserva."
        );

        imprimirPrecondiciones(
                "CP-R3-CU01",
                reservaUsuario.getUsuario().getNombres() + " "
                        + reservaUsuario.getUsuario().getApellidos(),
                reserva.getAmbiente().getCodigo(),
                "Reserva vigente",
                "No existe validación aceptada previa"
        );

        imprimirInstrucciones1();

        //ESPERAR EL MOVIMIENTO REAL DEL USUARIO
        ValidacionIngreso validacion = esperarValidacion(reservaUsuario);

        // CONTRASTAR RESULTADO ESPERADO Y REAL
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
                                "No se registró fecha y hora de validación."
                        )
        );

        // MOSTRAR RESULTADO REAL
        imprimirResultado(
                "CP-R3-CU01",
                "Validación registrada.",
                "Reserva: " + validacion.getReservaUsuario().getReserva().getCodigoReserva(),
                "Punto: " + validacion.getPuntoValidacion().getCodigoPunto(),
                "Medio: " + validacion.getMedioValidacion(),
                "Fecha y hora: " + validacion.getFechaHoraValidacion()
        );
    }

    //ESPERA DEL MOVIMIENTO REAL EN EL FRONT

    private ValidacionIngreso esperarValidacion(
            ReservaUsuario reservaUsuario)
            throws InterruptedException {

        long momentoLimite = System.currentTimeMillis() + TIEMPO_MAXIMO_ESPERA_MS;


        while (System.currentTimeMillis() < momentoLimite) {
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
                """
                CP-R3-CU01 - PRUEBA NO SUPERADA
                No se detectó la validación esperada en la base de datos.
                """
        );
    }


//BÚSQUEDA DEL RESULTADO EN BD
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


//SALIDA EN CONSOLA
    private void imprimirPrecondiciones(
            String codigoCaso,
            String estudiante,
            String ambiente,
            String... condiciones) {

        System.out.println("\nPrecondiciones " + codigoCaso + ":");
        System.out.println("- Estudiante: " + estudiante);
        System.out.println("- Ambiente: " + ambiente);
        for (String condicion : condiciones)  System.out.println( "- " + condicion);
        System.out.println();
    }


    private void imprimirInstrucciones1() {

        System.out.println(
                """
                Acción: Realizar desde el front el escaneo del DNI 10000001 en el punto PVAL-CCSS-001.
                Esperado: La validación debe ser aceptada y el ingreso registrado.
                
                Esperando acción desde el front...
                """
        );
    }


    private void imprimirResultado(
            String codigoCaso,
            String resultado,
            String... datos) {

        System.out.println("\nResultado real:");
        System.out.println(resultado);
        for (String dato : datos)  System.out.println("- " + dato);
        System.out.println("\n" + codigoCaso + " - PRUEBA SUPERADA");
        System.out.println("========================================"
        );
    }
}