package com.camila.moduloautomatizado.service.unit;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;

import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import com.camila.moduloautomatizado.service.ControlOcupacionService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ControlOcupacionUnitTest {


    @Mock
    private ControlOcupacionRepository
            controlOcupacionRepository;

    @Mock
    private OcupacionEstadoRepository
            ocupacionEstadoRepository;

    @Mock
    private ReservaEstadoRepository
            reservaEstadoRepository;

    @Mock
    private ValidacionIngresoRepository
            validacionIngresoRepository;

    @InjectMocks
    private ControlOcupacionService
            controlOcupacionService;


    // CP-R5-U01
    @Test
    @DisplayName("CP-R5-U01 - Evaluación de las condiciones de ocupación durante el periodo de tolerancia")
    void debeEvaluarCantidadDeUsuariosValidadosDuranteTolerancia() {

        LocalDateTime inicio = LocalDateTime.of(2026,10,2,10,0);
        LocalDateTime momento = inicio.plusMinutes(5);

        Ambiente ambiente = new Ambiente();

        ambiente.setIdAmbiente(1);
        ambiente.setCodigo("CCSS-AMB-001");
        ambiente.setCantidadMinima(3);

        Reserva reserva = crearReserva(1,"RES-R5-U01",ambiente,inicio);

        ReservaEstado estadoVigente = crearEstadoReservaVigente(reserva,inicio);

        ControlOcupacion control = crearControl(1,reserva,inicio);

        OcupacionEstado pendiente = crearEstadoOcupacion(control,reserva,EstadoOcupacion.PENDIENTE,inicio);

        long cantidadUsuariosValidados = 2L;


        when(
                reservaEstadoRepository.findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
        ).thenReturn(
                Optional.of(estadoVigente)
        );

        when(
                controlOcupacionRepository.findByReserva(reserva)
        ).thenReturn(
                Optional.of(control)
        );


        when(
                ocupacionEstadoRepository
                        .findTopByControlOcupacionAndFechaHoraInicioPeriodoAndFechaHoraFinPeriodoOrderByFechaHoraEstadoDesc(
                                control,
                                reserva.getFechaHoraInicio(),
                                reserva.getFechaHoraFin()
                        )
        ).thenReturn(
                Optional.of(pendiente)
        );

        when(
                validacionIngresoRepository .contarUsuariosValidadosPorReserva(reserva)
        ).thenReturn(
                cantidadUsuariosValidados
        );

        Optional<OcupacionEstado> resultado = controlOcupacionService.evaluarReserva(reserva,momento);

        assertTrue(resultado.isPresent());
        assertSame(pendiente,resultado.get());
        assertEquals(
                EstadoOcupacion.PENDIENTE,
                resultado.get().getEstadoOcupacion());
        assertTrue(
                cantidadUsuariosValidados < ambiente.getCantidadMinima()
        );

        verify(
                validacionIngresoRepository,
                times(1)
        ).contarUsuariosValidadosPorReserva(reserva);

        verify(
                ocupacionEstadoRepository,
                never()
        ).save(any(OcupacionEstado.class));

        System.out.println(
                """
                ========================================
                CP-R5-U01 - PRUEBA SUPERADA
                Usuarios únicos validados: %d
                Cantidad mínima requerida: %d
                Estado obtenido: %s
                Tolerancia vigente hasta: %s
                ========================================
                """.formatted(
                        cantidadUsuariosValidados,
                        ambiente.getCantidadMinima(),
                        resultado.get() .getEstadoOcupacion(),
                        control.getFechaHoraLimiteTolerancia()
                )
        );
    }


    // CP-R5-U02
    @Test
    @DisplayName("CP-R5-U02 - Determinación del estado de ocupación")
    void debeDeterminarOcupadoOLiberadoSegunCondiciones() {

        LocalDateTime inicio = LocalDateTime.of(2026,10,2,12,0);

        Ambiente ambienteOcupado = new Ambiente();
        ambienteOcupado.setIdAmbiente(1);
        ambienteOcupado.setCodigo("CCSS-AMB-001");
        ambienteOcupado.setCantidadMinima(3);

        Reserva reservaOcupada =
                crearReserva(
                        1,
                        "RES-R5-U02-O",
                        ambienteOcupado,
                        inicio
                );

        ControlOcupacion controlOcupado = crearControl(
                1,
                reservaOcupada,
                inicio
        );

        OcupacionEstado pendienteOcupado =
                crearEstadoOcupacion(
                        controlOcupado,
                        reservaOcupada,
                        EstadoOcupacion.PENDIENTE,
                        inicio
                );

        Ambiente ambienteLiberado = new Ambiente();
        ambienteLiberado.setIdAmbiente(2);
        ambienteLiberado.setCodigo("CCSS-AMB-002");
        ambienteLiberado.setCantidadMinima(3);

        Reserva reservaLiberada =
                crearReserva(
                        2,
                        "RES-R5-U02-L",
                        ambienteLiberado,inicio
                );


        ControlOcupacion controlLiberado =
                crearControl(
                        2,
                        reservaLiberada,
                        inicio
                );

        OcupacionEstado pendienteLiberado =
                crearEstadoOcupacion(
                        controlLiberado,
                        reservaLiberada,
                        EstadoOcupacion.PENDIENTE,
                        inicio
                );

        ReservaEstado vigenteOcupada =
                crearEstadoReservaVigente(
                        reservaOcupada,
                        inicio
                );

        ReservaEstado vigenteLiberada =
                crearEstadoReservaVigente(
                        reservaLiberada,
                        inicio
                );

        when(
                reservaEstadoRepository
                        .findTopByReservaOrderByFechaHoraEstadoDesc(reservaOcupada)
        ).thenReturn(
                Optional.of(vigenteOcupada)
        );

        when(
                reservaEstadoRepository
                        .findTopByReservaOrderByFechaHoraEstadoDesc(reservaLiberada)
        ).thenReturn(
                Optional.of(vigenteLiberada)
        );

        when(
                controlOcupacionRepository
                        .findByReserva(reservaOcupada)
        ).thenReturn(
                Optional.of(controlOcupado)
        );


        when(
                controlOcupacionRepository
                        .findByReserva(reservaLiberada)
        ).thenReturn(
                Optional.of(controlLiberado)
        );

        when(
                ocupacionEstadoRepository
                        .findTopByControlOcupacionAndFechaHoraInicioPeriodoAndFechaHoraFinPeriodoOrderByFechaHoraEstadoDesc(
                                controlOcupado,
                                reservaOcupada.getFechaHoraInicio(),
                                reservaOcupada.getFechaHoraFin()
                        )
        ).thenReturn(
                Optional.of(pendienteOcupado)
        );

        when(
                ocupacionEstadoRepository
                        .findTopByControlOcupacionAndFechaHoraInicioPeriodoAndFechaHoraFinPeriodoOrderByFechaHoraEstadoDesc(
                                controlLiberado,
                                reservaLiberada.getFechaHoraInicio(),
                                reservaLiberada.getFechaHoraFin()
                        )
        ).thenReturn(
                Optional.of(pendienteLiberado)
        );


        when(
                validacionIngresoRepository
                        .contarUsuariosValidadosPorReserva(
                                reservaOcupada
                        )
        ).thenReturn(
                3L
        );


        when(
                validacionIngresoRepository
                        .contarUsuariosValidadosPorReserva(
                                reservaLiberada
                        )
        ).thenReturn(
                2L
        );

        when(
                ocupacionEstadoRepository.save(any(OcupacionEstado.class))
        ).thenAnswer(
                invocacion ->  invocacion.getArgument(0)
        );


        LocalDateTime momentoOcupacion = inicio.plusMinutes(5);

        LocalDateTime momentoLiberacion = controlLiberado.getFechaHoraLimiteTolerancia();

        Optional<OcupacionEstado> resultadoOcupado =
                controlOcupacionService
                        .evaluarReserva(
                                reservaOcupada,
                                momentoOcupacion
                        );

        Optional<OcupacionEstado> resultadoLiberado =
                controlOcupacionService
                        .evaluarReserva(
                                reservaLiberada,
                                momentoLiberacion
                        );

        assertTrue(resultadoOcupado.isPresent());
        assertTrue(resultadoLiberado.isPresent());

        assertEquals(
                EstadoOcupacion.OCUPADO,
                resultadoOcupado.get().getEstadoOcupacion()
        );

        assertEquals(
                EstadoOcupacion.LIBERADO,
                resultadoLiberado.get().getEstadoOcupacion()
        );

        assertEquals(
                momentoOcupacion,
                resultadoOcupado.get().getFechaHoraEstado()
        );

        assertEquals(
                momentoLiberacion,
                resultadoLiberado.get().getFechaHoraEstado()
        );


        verify(
                ocupacionEstadoRepository,
                times(2)
        ).save(
                any(OcupacionEstado.class)
        );


        System.out.println(
                """
                ========================================
                CP-R5-U02 - PRUEBA SUPERADA
                Escenario 1:
                Usuarios validados: 3
                Mínimo requerido: 3
                Estado obtenido: %s
                
                Escenario 2:
                Usuarios validados: 2
                Mínimo requerido: 3
                Tolerancia finalizada: %s
                Estado obtenido: %s
                ========================================
                """.formatted(
                        resultadoOcupado
                                .get()
                                .getEstadoOcupacion(),
                        momentoLiberacion,
                        resultadoLiberado
                                .get()
                                .getEstadoOcupacion()
                )
        );
    }


    private Reserva crearReserva(
            Integer idReserva,
            String codigoReserva,
            Ambiente ambiente,
            LocalDateTime inicio) {

        Reserva reserva = new Reserva();
        reserva.setIdReserva(idReserva);
        reserva.setCodigoReserva(codigoReserva);
        reserva.setAmbiente(ambiente);
        reserva.setFechaHoraInicio(inicio);
        reserva.setFechaHoraFin(inicio.plusHours(1));
        reserva.setToleranciaMinutos(15);
        reserva.setFechaCreacion(inicio.minusHours(1));

        return reserva;
    }


    private ReservaEstado crearEstadoReservaVigente(
            Reserva reserva,
            LocalDateTime momento) {

        ReservaEstado estado = new ReservaEstado();
        estado.setReserva(reserva);
        estado.setEstadoReserva(EstadoReserva.VIGENTE);
        estado.setFechaHoraEstado(momento);
        estado.setFechaCreacion(momento);

        return estado;
    }


    private ControlOcupacion crearControl(
            Integer idControl,
            Reserva reserva,
            LocalDateTime inicio) {

        ControlOcupacion control = new ControlOcupacion();
        control.setIdControl(idControl);
        control.setReserva(reserva);
        control.setFechaHoraInicioControl(inicio);
        control.setFechaHoraLimiteTolerancia(inicio.plusMinutes(15));
        control.setFechaCreacion(inicio);

        return control;
    }


    private OcupacionEstado crearEstadoOcupacion(
            ControlOcupacion control,
            Reserva reserva,
            EstadoOcupacion estadoOcupacion,
            LocalDateTime momento) {

        OcupacionEstado estado = new OcupacionEstado();

        estado.setControlOcupacion(control);
        estado.setEstadoOcupacion(estadoOcupacion);
        estado.setFechaHoraInicioPeriodo(reserva.getFechaHoraInicio());
        estado.setFechaHoraFinPeriodo(reserva.getFechaHoraFin());
        estado.setFechaHoraEstado(momento);
        estado.setFechaCreacion(momento);
        estado.setMotivo("Estado preparado para prueba unitaria");

        return estado;
    }
}