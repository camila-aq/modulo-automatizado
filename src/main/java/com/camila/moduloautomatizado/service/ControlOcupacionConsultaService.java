package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.dto.ControlOcupacionResponse;
import com.camila.moduloautomatizado.dto.OcupacionEstadoResponse;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;

import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ControlOcupacionConsultaService {

    private final ReservaRepository reservaRepository;
    private final ControlOcupacionRepository controlOcupacionRepository;
    private final OcupacionEstadoRepository ocupacionEstadoRepository;


    public ControlOcupacionConsultaService(
            ReservaRepository reservaRepository,
            ControlOcupacionRepository controlOcupacionRepository,
            OcupacionEstadoRepository ocupacionEstadoRepository) {

        this.reservaRepository = reservaRepository;
        this.controlOcupacionRepository = controlOcupacionRepository;
        this.ocupacionEstadoRepository = ocupacionEstadoRepository;
    }


    @Transactional(readOnly = true)
    public ControlOcupacionResponse obtenerControl(
            Integer idReserva) {

        Reserva reserva =
                reservaRepository
                        .findById(idReserva)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la reserva."
                                )
                        );

        ControlOcupacion controlOcupacion =
                controlOcupacionRepository
                        .findByReserva(reserva)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La reserva aún no cuenta con un control de ocupación."
                                )
                        );

        List<OcupacionEstado> estadosAutomaticos =
                ocupacionEstadoRepository
                        .findByControlOcupacionOrderByFechaHoraEstadoAsc(controlOcupacion)
                        .stream()
                        .filter(estado ->
                                reserva.getFechaHoraInicio().equals(estado.getFechaHoraInicioPeriodo())
                                && reserva.getFechaHoraFin().equals(estado.getFechaHoraFinPeriodo())
                        )
                        .toList();

        List<OcupacionEstadoResponse> historial =
                estadosAutomaticos
                        .stream()
                        .map(estado ->
                                new OcupacionEstadoResponse(
                                        estado.getEstadoOcupacion().name(),
                                        estado.getFechaHoraInicioPeriodo(),
                                        estado.getFechaHoraFinPeriodo(),
                                        estado.getMotivo(),
                                        estado.getFechaHoraEstado()
                                )
                        )
                        .toList();

        String estadoActual =
                estadosAutomaticos.isEmpty()
                        ? null
                        : estadosAutomaticos
                        .get(estadosAutomaticos.size() - 1)
                        .getEstadoOcupacion()
                        .name();

        return new ControlOcupacionResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                controlOcupacion.getFechaHoraInicioControl(),
                controlOcupacion.getFechaHoraLimiteTolerancia(),
                estadoActual,
                historial
        );
    }
}