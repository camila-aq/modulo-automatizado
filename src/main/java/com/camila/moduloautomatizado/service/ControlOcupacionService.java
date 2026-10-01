package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;

import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ControlOcupacionService {

    private final ControlOcupacionRepository controlOcupacionRepository;

    private final OcupacionEstadoRepository ocupacionEstadoRepository;

    private final ReservaEstadoRepository reservaEstadoRepository;

    private final ValidacionIngresoRepository validacionIngresoRepository;


    public ControlOcupacionService(
            ControlOcupacionRepository controlOcupacionRepository,
            OcupacionEstadoRepository ocupacionEstadoRepository,
            ReservaEstadoRepository reservaEstadoRepository,
            ValidacionIngresoRepository validacionIngresoRepository) {

        this.controlOcupacionRepository = controlOcupacionRepository;
        this.ocupacionEstadoRepository = ocupacionEstadoRepository;
        this.reservaEstadoRepository = reservaEstadoRepository;
        this.validacionIngresoRepository = validacionIngresoRepository;
    }

    @Transactional
    public Optional<OcupacionEstado> evaluarReserva(
            Reserva reserva,
            LocalDateTime momento) {

        if (reserva == null) {

            throw new IllegalArgumentException(
                    "La reserva es obligatoria."
            );
        }

        if (momento == null) {

            throw new IllegalArgumentException(
                    "El momento de evaluación es obligatorio."
            );
        }

        if (momento.isBefore(reserva.getFechaHoraInicio())) {

            return obtenerEstadoAutomaticoExistente(reserva);
        }

        if (!reservaEstaVigente(reserva)) {

            return obtenerEstadoAutomaticoExistente(reserva);
        }

        ControlOcupacion controlOcupacion = obtenerOCrearControlOcupacion(reserva,momento);

        Optional<OcupacionEstado> estadoExistente = obtenerUltimoEstadoAutomatico(controlOcupacion,reserva);

        if (estadoExistente.isPresent()) {

            EstadoOcupacion estadoActual = estadoExistente.get().getEstadoOcupacion();

            if (estadoActual == EstadoOcupacion.OCUPADO || estadoActual == EstadoOcupacion.LIBERADO) {
                return estadoExistente;
            }
        }


        OcupacionEstado estadoPendiente;

        if (estadoExistente.isEmpty()) {

            estadoPendiente =
                    registrarEstado(
                            controlOcupacion,
                            reserva,
                            EstadoOcupacion.PENDIENTE,
                            "Inicio del control automático de ocupación",
                            reserva.getFechaHoraInicio(),
                            momento
                    );

        } else {
            estadoPendiente = estadoExistente.get();
        }

        long cantidadUsuariosValidados = validacionIngresoRepository.contarUsuariosValidadosPorReserva(reserva);

        int cantidadMinima = reserva.getAmbiente().getCantidadMinima();

        LocalDateTime limiteTolerancia = controlOcupacion.getFechaHoraLimiteTolerancia();

        boolean minimoAlcanzado = cantidadUsuariosValidados >= cantidadMinima;

        boolean dentroDeTolerancia = !momento.isAfter(limiteTolerancia);

        if (minimoAlcanzado && dentroDeTolerancia) {

            OcupacionEstado estadoOcupado =
                    registrarEstado(
                            controlOcupacion,
                            reserva,
                            EstadoOcupacion.OCUPADO,
                            "Cantidad mínima de usuarios validada",
                            momento,
                            momento
                    );

            return Optional.of(estadoOcupado);
        }

        boolean toleranciaFinalizada = !momento.isBefore(limiteTolerancia);

        if (toleranciaFinalizada) {

            OcupacionEstado estadoLiberado =
                    registrarEstado(
                            controlOcupacion,
                            reserva,
                            EstadoOcupacion.LIBERADO,
                            "Cantidad mínima no alcanzada dentro del tiempo de tolerancia",
                            momento,
                            momento
                    );

            return Optional.of(estadoLiberado);
        }

        return Optional.of(estadoPendiente);
    }


    private boolean reservaEstaVigente(
            Reserva reserva) {

        return reservaEstadoRepository
                .findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
                .map(estado ->
                        estado.getEstadoReserva() == EstadoReserva.VIGENTE
                )
                .orElse(false);
    }

    private ControlOcupacion obtenerOCrearControlOcupacion(
            Reserva reserva,
            LocalDateTime momento) {

        return controlOcupacionRepository
                .findByReserva(reserva)
                .orElseGet(() -> {

                    ControlOcupacion control = new ControlOcupacion();
                    control.setReserva(reserva);
                    control.setFechaHoraInicioControl(reserva.getFechaHoraInicio());
                    control.setFechaHoraLimiteTolerancia(
                            reserva.getFechaHoraInicio().plusMinutes(reserva.getToleranciaMinutos())
                    );
                    control.setFechaCreacion(momento);
                    control.setUsuarioCreacion(null);

                    return controlOcupacionRepository.save(control);
                });
    }

    private Optional<OcupacionEstado>
    obtenerEstadoAutomaticoExistente(
            Reserva reserva) {

        return controlOcupacionRepository
                .findByReserva(reserva)
                .flatMap(control ->
                        obtenerUltimoEstadoAutomatico(
                                control,
                                reserva
                        )
                );
    }

    private Optional<OcupacionEstado>
    obtenerUltimoEstadoAutomatico(
            ControlOcupacion controlOcupacion,
            Reserva reserva) {

        return ocupacionEstadoRepository
                .findTopByControlOcupacionAndFechaHoraInicioPeriodoAndFechaHoraFinPeriodoOrderByFechaHoraEstadoDesc(
                        controlOcupacion,
                        reserva.getFechaHoraInicio(),
                        reserva.getFechaHoraFin()
                );
    }

    private OcupacionEstado registrarEstado(
            ControlOcupacion controlOcupacion,
            Reserva reserva,
            EstadoOcupacion estadoOcupacion,
            String motivo,
            LocalDateTime fechaHoraEstado,
            LocalDateTime fechaCreacion) {

        OcupacionEstado estado = new OcupacionEstado();
        estado.setControlOcupacion(controlOcupacion);
        estado.setEstadoOcupacion(estadoOcupacion);
        estado.setFechaHoraInicioPeriodo(reserva.getFechaHoraInicio());
        estado.setFechaHoraFinPeriodo(reserva.getFechaHoraFin());
        estado.setMotivo(motivo);
        estado.setFechaHoraEstado(fechaHoraEstado);
        estado.setFechaCreacion(fechaCreacion);
        estado.setUsuarioCreacion(null);

        return ocupacionEstadoRepository.save( estado);
    }
}