package com.camila.moduloautomatizado.service.unit;

import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.model.enums.RolEnReserva;
import com.camila.moduloautomatizado.model.enums.TipoCorreo;

import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;

import com.camila.moduloautomatizado.service.ControlOcupacionOrquestadorService;
import com.camila.moduloautomatizado.service.ControlOcupacionService;
import com.camila.moduloautomatizado.service.CorreoElectronicoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ControlOcupacionTrazabilidadUnitTest {

    @Mock
    private ControlOcupacionService controlOcupacionService;

    @Mock
    private CorreoElectronicoService correoElectronicoService;

    @Mock
    private ReservaUsuarioRepository reservaUsuarioRepository;

    @InjectMocks
    private ControlOcupacionOrquestadorService controlOcupacionOrquestadorService;


    // CP-R5-U03
    @Test
    @DisplayName("CP-R5-U03 - Registro de trazabilidad y comunicación asociada")
    void debeRegistrarTrazabilidadYComunicacionAsociada() {

        LocalDateTime momentoOcupacion =
                LocalDateTime.of(
                        2026,
                        10,
                        2,
                        14,
                        5
                );

        LocalDateTime momentoLiberacion =
                LocalDateTime.of(
                        2026,
                        10,
                        2,
                        15,
                        15
                );

        Reserva reservaOcupada = new Reserva();
        reservaOcupada.setIdReserva(1);
        reservaOcupada.setCodigoReserva("RES-R5-U03-O");

        Usuario responsableOcupado = new Usuario();
        responsableOcupado.setIdUsuario(1);
        responsableOcupado.setDni("10000001");

        ReservaUsuario asociacionOcupada = new ReservaUsuario();
        asociacionOcupada.setReserva(reservaOcupada);
        asociacionOcupada.setUsuario(responsableOcupado);
        asociacionOcupada.setRolEnReserva(RolEnReserva.RESPONSABLE);
        asociacionOcupada.setActivo(true);

        OcupacionEstado estadoOcupado = new OcupacionEstado();
        estadoOcupado.setEstadoOcupacion(EstadoOcupacion.OCUPADO);
        estadoOcupado.setFechaHoraEstado(momentoOcupacion);


        Reserva reservaLiberada = new Reserva();
        reservaLiberada.setIdReserva(2);
        reservaLiberada.setCodigoReserva("RES-R5-U03-L");

        Usuario responsableLiberado = new Usuario();
        responsableLiberado.setIdUsuario(2);
        responsableLiberado.setDni("10000002");

        ReservaUsuario asociacionLiberada = new ReservaUsuario();
        asociacionLiberada.setReserva(reservaLiberada);
        asociacionLiberada.setUsuario(responsableLiberado);
        asociacionLiberada.setRolEnReserva(RolEnReserva.RESPONSABLE);
        asociacionLiberada.setActivo(true);

        OcupacionEstado estadoLiberado = new OcupacionEstado();
        estadoLiberado.setEstadoOcupacion(EstadoOcupacion.LIBERADO);
        estadoLiberado.setFechaHoraEstado(momentoLiberacion);

        when(controlOcupacionService
                .evaluarReserva(reservaOcupada,momentoOcupacion))
                .thenReturn(Optional.of(estadoOcupado));

        when(controlOcupacionService
                .evaluarReserva(reservaLiberada,momentoLiberacion))
                .thenReturn(Optional.of(estadoLiberado));

        when(reservaUsuarioRepository
                .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(reservaOcupada))
                .thenReturn(List.of(asociacionOcupada));

        when(reservaUsuarioRepository
                .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(reservaLiberada))
                .thenReturn(List.of(asociacionLiberada));

        when(correoElectronicoService
                .registrarSiNoExiste(
                        reservaOcupada,
                        responsableOcupado,
                        TipoCorreo.OCUPACION_CONFIRMADA,
                        momentoOcupacion
                ))
                .thenReturn(Optional.empty());

        when(correoElectronicoService
                .registrarSiNoExiste(
                        reservaLiberada,
                        responsableLiberado,
                        TipoCorreo.LIBERACION_AUTOMATICA,
                        momentoLiberacion
                ))
                .thenReturn(Optional.empty());

        Optional<OcupacionEstado> resultadoOcupado =
                controlOcupacionOrquestadorService
                        .procesarReserva(
                                reservaOcupada,
                                momentoOcupacion
                        );

        Optional<OcupacionEstado> resultadoLiberado =
                controlOcupacionOrquestadorService
                        .procesarReserva(
                                reservaLiberada,
                                momentoLiberacion
                        );

        assertTrue(resultadoOcupado.isPresent());
        assertTrue(resultadoLiberado.isPresent());

        assertSame(estadoOcupado,resultadoOcupado.get());
        assertSame(estadoLiberado,resultadoLiberado.get());

        assertEquals(
                momentoOcupacion,
                resultadoOcupado.get().getFechaHoraEstado()
        );

        assertEquals(
                momentoLiberacion,
                resultadoLiberado.get().getFechaHoraEstado()
        );

        verify(correoElectronicoService,times(1))
                .registrarSiNoExiste(
                        reservaOcupada,
                        responsableOcupado,
                        TipoCorreo.OCUPACION_CONFIRMADA,
                        momentoOcupacion
                );

        verify(correoElectronicoService,times(1))
                .registrarSiNoExiste(
                        reservaLiberada,
                        responsableLiberado,
                        TipoCorreo.LIBERACION_AUTOMATICA,
                        momentoLiberacion
                );

        System.out.println("""
                ========================================
                CP-R5-U03 - PRUEBA SUPERADA
                Cambio de estado 1:
                Estado: %s
                Comunicación asociada: %s
                Fecha y hora: %s
                
                Cambio de estado 2:
                Estado: %s
                Comunicación asociada: %s
                Fecha y hora: %s
                ========================================
                """.formatted(
                resultadoOcupado.get().getEstadoOcupacion(),
                TipoCorreo.OCUPACION_CONFIRMADA,
                momentoOcupacion,
                resultadoLiberado.get().getEstadoOcupacion(),
                TipoCorreo.LIBERACION_AUTOMATICA,
                momentoLiberacion
        ));
    }
}