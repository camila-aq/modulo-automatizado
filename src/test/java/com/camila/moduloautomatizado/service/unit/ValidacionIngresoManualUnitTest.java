package com.camila.moduloautomatizado.service.unit;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Ubicacion;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import com.camila.moduloautomatizado.service.ValidacionIngresoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ValidacionIngresoManualUnitTest {


    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReservaUsuarioRepository reservaUsuarioRepository;

    @Mock
    private ReservaEstadoRepository reservaEstadoRepository;

    @Mock
    private PuntoValidacionRepository puntoValidacionRepository;

    @Mock
    private ValidacionIngresoRepository validacionIngresoRepository;

    @InjectMocks
    private ValidacionIngresoService validacionIngresoService;


    // CP-R3-U02
    @Test
    @DisplayName("CP-R3-U02 - Validación de ingreso manual")
    void debeRegistrarValidacionManualCuandoLasCondicionesSonValidas() {

        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setDni("10000001");
        usuario.setCodigoUniversitario("EST0000001");

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setIdUbicacion(1);
        ubicacion.setNombre("COMPLEJO DE CIENCIAS SOCIALES");

        Ambiente ambiente = new Ambiente();
        ambiente.setIdAmbiente(1);
        ambiente.setCodigo("CCSS-AMB-001");
        ambiente.setUbicacion(ubicacion);

        PuntoValidacion puntoValidacion = new PuntoValidacion();
        puntoValidacion.setIdPuntoValidacion(1);
        puntoValidacion.setCodigoPunto("PVAL-CCSS-001");
        puntoValidacion.setAmbiente(ambiente);
        puntoValidacion.setActivo(true);

        Reserva reserva = new Reserva();
        reserva.setIdReserva(1);
        reserva.setCodigoReserva("RES-MAN-UNIT-001");
        reserva.setAmbiente(ambiente);
        reserva.setFechaHoraInicio(ahora.minusMinutes(5));
        reserva.setFechaHoraFin(ahora.plusMinutes(30));

        ReservaUsuario reservaUsuario = new ReservaUsuario();
        reservaUsuario.setIdReservaUsuario(1);
        reservaUsuario.setUsuario(usuario);
        reservaUsuario.setReserva(reserva);
        reservaUsuario.setActivo(true);

        ReservaEstado estadoVigente = new ReservaEstado();
        estadoVigente.setReserva(reserva);
        estadoVigente.setEstadoReserva(EstadoReserva.VIGENTE);
        estadoVigente.setFechaHoraEstado(ahora);

        when(usuarioRepository.
                findByDni("10000001")).
                thenReturn(Optional.of(usuario));

        when(reservaUsuarioRepository.
                findByUsuarioAndActivoTrue(usuario)).
                thenReturn(List.of(reservaUsuario));

        when(reservaEstadoRepository.
                findTopByReservaOrderByFechaHoraEstadoDesc(reserva)).
                thenReturn(Optional.of(estadoVigente));

        when(reservaUsuarioRepository.
                findById(1)).
                thenReturn(Optional.of(reservaUsuario));

        when(validacionIngresoRepository.
                existsByReservaUsuario(reservaUsuario)).
                thenReturn(false);


        when(puntoValidacionRepository.
                findByAmbienteAndActivoTrue(ambiente)).
                thenReturn(Optional.of(puntoValidacion));

        when(validacionIngresoRepository.
                save(any(ValidacionIngreso.class))).
                thenAnswer(invocacion ->invocacion.getArgument(0));


        Usuario usuarioIdentificado =
                validacionIngresoService.identificarUsuarioPorDni(
                        "10000001"
                );


        ReservaUsuario reservaEncontrada =
                validacionIngresoService.buscarReservaVigenteParaValidacionManual(
                        usuarioIdentificado,
                        ubicacion.getIdUbicacion()
                );


        ValidacionIngreso resultado =
                validacionIngresoService.confirmarValidacionManual(
                        reservaEncontrada.getIdReservaUsuario(),
                        TipoIdentificador.DNI,
                        ubicacion.getIdUbicacion()
                );


        ArgumentCaptor<ValidacionIngreso> captor =
                ArgumentCaptor.forClass(ValidacionIngreso.class);

        verify(validacionIngresoRepository).save(captor.capture());

        ValidacionIngreso validacionRegistrada = captor.getValue();


        assertNotNull(resultado);
        assertSame(usuario,usuarioIdentificado);
        assertSame(reservaUsuario,reservaEncontrada);

        assertSame(
                usuario,
                validacionRegistrada.getReservaUsuario().getUsuario()
        );

        assertSame(
                reserva,
                validacionRegistrada.getReservaUsuario().getReserva()
        );

        assertSame(
                ubicacion,
                validacionRegistrada
                        .getReservaUsuario()
                        .getReserva()
                        .getAmbiente()
                        .getUbicacion()
        );

        assertEquals(
                ubicacion.getIdUbicacion(),
                validacionRegistrada
                        .getReservaUsuario()
                        .getReserva()
                        .getAmbiente()
                        .getUbicacion()
                        .getIdUbicacion()
        );

        assertSame(
                puntoValidacion,
                validacionRegistrada.getPuntoValidacion()
        );

        assertEquals(
                MedioValidacion.INGRESO_MANUAL,
                validacionRegistrada.getMedioValidacion()
        );

        assertEquals(
                TipoIdentificador.DNI,
                validacionRegistrada.getTipoIdentificador()
        );

        assertNotNull(validacionRegistrada.getFechaHoraValidacion());


        System.out.println("""
                ========================================
                CP-R3-U02 - PRUEBA SUPERADA
                Medio de validación: %s
                Tipo de identificador: %s
                DNI del usuario: %s
                Ubicación: %s
                Reserva: %s
                Ambiente: %s
                Punto de validación: %s
                Fecha y hora de validación: %s
                ========================================
                """.formatted(
                validacionRegistrada
                        .getMedioValidacion(),
                validacionRegistrada
                        .getTipoIdentificador(),
                validacionRegistrada
                        .getReservaUsuario()
                        .getUsuario()
                        .getDni(),
                validacionRegistrada
                        .getReservaUsuario()
                        .getReserva()
                        .getAmbiente()
                        .getUbicacion()
                        .getNombre(),
                validacionRegistrada
                        .getReservaUsuario()
                        .getReserva()
                        .getCodigoReserva(),
                validacionRegistrada
                        .getReservaUsuario()
                        .getReserva()
                        .getAmbiente()
                        .getCodigo(),
                validacionRegistrada
                        .getPuntoValidacion()
                        .getCodigoPunto(),
                validacionRegistrada
                        .getFechaHoraValidacion()
        ));
    }
}