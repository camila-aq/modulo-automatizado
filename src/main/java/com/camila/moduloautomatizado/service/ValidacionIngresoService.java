package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;


import java.time.LocalDateTime;

import java.util.Objects;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidacionIngresoService {

    private final UsuarioRepository usuarioRepository;
    private final ReservaUsuarioRepository reservaUsuarioRepository;
    private final ReservaEstadoRepository reservaEstadoRepository;
    private final PuntoValidacionRepository puntoValidacionRepository;
    private final ValidacionIngresoRepository validacionIngresoRepository;

    public ValidacionIngresoService(
            UsuarioRepository usuarioRepository,
            ReservaUsuarioRepository reservaUsuarioRepository,
            ReservaEstadoRepository reservaEstadoRepository,
            PuntoValidacionRepository puntoValidacionRepository,
            ValidacionIngresoRepository validacionIngresoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.reservaUsuarioRepository = reservaUsuarioRepository;
        this.reservaEstadoRepository = reservaEstadoRepository;
        this.puntoValidacionRepository = puntoValidacionRepository;
        this.validacionIngresoRepository = validacionIngresoRepository;
    }

    @Transactional
    public ValidacionIngreso validarIngresoPorEscaneo(String dni, String codigoPunto) {

        Usuario usuario = obtenerUsuarioPorDni(dni);
        PuntoValidacion puntoValidacion = obtenerPuntoValidacion(codigoPunto);

        List<ReservaUsuario> reservasUsuario = reservaUsuarioRepository.findByUsuarioAndActivoTrue(usuario);

        if (reservasUsuario.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no se encuentra asociado a ninguna reserva."
            );
        }

        LocalDateTime momento = LocalDateTime.now();

        List<ReservaUsuario> reservasVigentes = reservasUsuario.stream()
                .filter(reservaUsuario -> reservaEstaVigente(reservaUsuario, momento))
                .toList();

        if (reservasVigentes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no se encuentra asociado a una reserva vigente."
            );
        }
        ReservaUsuario reservaUsuarioValida = reservasVigentes.stream()
                .filter(reservaUsuario ->
                        Objects.equals(
                                reservaUsuario.getReserva().getAmbiente().getIdAmbiente(),
                                puntoValidacion.getAmbiente().getIdAmbiente()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La reserva vigente del usuario no corresponde al ambiente."
                        )
                );
        if (validacionIngresoRepository.existsByReservaUsuario(reservaUsuarioValida)) {
            throw new IllegalArgumentException(
                    "El usuario ya cuenta con una validación aceptada para esta reserva."
            );
        }
        ValidacionIngreso validacionIngreso = new ValidacionIngreso();

        validacionIngreso.setPuntoValidacion(puntoValidacion);
        validacionIngreso.setReservaUsuario(reservaUsuarioValida);
        validacionIngreso.setMedioValidacion(MedioValidacion.ESCANEO_SIMULADO);
        validacionIngreso.setTipoIdentificador(TipoIdentificador.DNI);
        validacionIngreso.setFechaHoraValidacion(momento);
        validacionIngreso.setFechaCreacion(momento);

        return validacionIngresoRepository.save(validacionIngreso);
    }

    private boolean reservaEstaVigente(
            ReservaUsuario reservaUsuario,
            LocalDateTime momento) {

        Reserva reserva = reservaUsuario.getReserva();

        boolean dentroDelHorario =
                !momento.isBefore(reserva.getFechaHoraInicio())
                        && !momento.isAfter(reserva.getFechaHoraFin());

        if (!dentroDelHorario) {
            return false;
        }

        return reservaEstadoRepository
                .findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
                .map(estado ->
                        estado.getEstadoReserva() == EstadoReserva.VIGENTE
                )
                .orElse(false);
    }

    private Usuario obtenerUsuarioPorDni(String dni) {
        return usuarioRepository.findByDni(dni)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El DNI no corresponde a un usuario registrado."
                        )
                );
    }

    private PuntoValidacion obtenerPuntoValidacion(String codigoPunto) {
        return puntoValidacionRepository.findByCodigoPuntoAndActivoTrue(codigoPunto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El punto de validación no existe o se encuentra inactivo."
                        )
                );
    }
}