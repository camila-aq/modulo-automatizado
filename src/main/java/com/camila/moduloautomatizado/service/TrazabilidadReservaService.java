package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.dto.EventoTrazabilidadResponse;
import com.camila.moduloautomatizado.dto.TrazabilidadReservaResponse;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.CorreoElectronicoRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TrazabilidadReservaService {

    private final ReservaRepository reservaRepository;
    private final ValidacionIngresoRepository validacionIngresoRepository;
    private final ControlOcupacionRepository controlOcupacionRepository;
    private final OcupacionEstadoRepository ocupacionEstadoRepository;
    private final CorreoElectronicoRepository correoElectronicoRepository;

    public TrazabilidadReservaService(
            ReservaRepository reservaRepository,
            ValidacionIngresoRepository validacionIngresoRepository,
            ControlOcupacionRepository controlOcupacionRepository,
            OcupacionEstadoRepository ocupacionEstadoRepository,
            CorreoElectronicoRepository correoElectronicoRepository) {

        this.reservaRepository = reservaRepository;
        this.validacionIngresoRepository = validacionIngresoRepository;
        this.controlOcupacionRepository = controlOcupacionRepository;
        this.ocupacionEstadoRepository = ocupacionEstadoRepository;
        this.correoElectronicoRepository = correoElectronicoRepository;
    }

    @Transactional(readOnly = true)
    public TrazabilidadReservaResponse obtenerTrazabilidad(
            Integer idReserva) {

        Reserva reserva =
                reservaRepository
                        .findById(idReserva)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la reserva."
                                )
                        );

        List<EventoTrazabilidadResponse> eventos = new ArrayList<>();

        agregarValidaciones(reserva,eventos);

        Optional<ControlOcupacion> control = controlOcupacionRepository.findByReserva(reserva);

        String estadoActual = null;

        LocalDateTime limiteTolerancia = null;


        if (control.isPresent()) {

            ControlOcupacion controlOcupacion = control.get();

            limiteTolerancia = controlOcupacion.getFechaHoraLimiteTolerancia();

            List<OcupacionEstado> estados =
                    ocupacionEstadoRepository
                            .findByControlOcupacionOrderByFechaHoraEstadoAsc(controlOcupacion)
                            .stream()
                            .filter(estado ->
                                    reserva.getFechaHoraInicio().equals(estado.getFechaHoraInicioPeriodo())
                                    &&
                                    reserva.getFechaHoraFin().equals(estado.getFechaHoraFinPeriodo())
                            )
                            .toList();

            for (OcupacionEstado estado : estados) {
                eventos.add(
                        new EventoTrazabilidadResponse(
                                "ESTADO_OCUPACION",
                                estado.getFechaHoraEstado(),
                                estado.getEstadoOcupacion().name() + " - "+ estado.getMotivo()
                        )
                );
            }

            if (!estados.isEmpty()) {
                estadoActual = estados.get(estados.size() - 1).getEstadoOcupacion().name();
            }
        }

        agregarCorreos(reserva,eventos);

        eventos.sort(
                Comparator.comparing(
                        EventoTrazabilidadResponse::fechaHora
                )
        );

        return new TrazabilidadReservaResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                estadoActual,
                limiteTolerancia,
                eventos
        );
    }


    private void agregarValidaciones(
            Reserva reserva,
            List<EventoTrazabilidadResponse> eventos) {

        List<ValidacionIngreso> validaciones =
                validacionIngresoRepository
                        .findValidacionesIngresoPorReserva(reserva);

        for (ValidacionIngreso validacion : validaciones) {

            Usuario usuario = validacion.getReservaUsuario().getUsuario();

            String detalle =
                    usuario.getCodigoUniversitario()
                            + " - "
                            + usuario.getNombres().toUpperCase(Locale.ROOT)
                            + " "
                            + usuario.getApellidos().toUpperCase(Locale.ROOT);

            eventos.add(
                    new EventoTrazabilidadResponse(
                            "VALIDACION_INGRESO",
                            validacion.getFechaHoraValidacion(),
                            detalle
                    )
            );
        }
    }

    private void agregarCorreos(
            Reserva reserva,
            List<EventoTrazabilidadResponse> eventos) {

        correoElectronicoRepository
                .findByReservaOrderByFechaEnvioAsc(reserva)
                .forEach(correo ->
                        eventos.add(
                                new EventoTrazabilidadResponse(
                                        "CORREO_ELECTRONICO",
                                        correo.getFechaEnvio(),
                                        correo.getTipoCorreo() .name()
                                )
                        )
                );
    }
}