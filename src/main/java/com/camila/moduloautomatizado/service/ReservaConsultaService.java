package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.dto.DetalleReservaResponse;
import com.camila.moduloautomatizado.dto.IntegranteReservaResponse;
import com.camila.moduloautomatizado.dto.PeriodoOcupacionResponse;
import com.camila.moduloautomatizado.dto.ReservaGrillaResponse;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.RolEnReserva;

import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservaConsultaService {

    private final ReservaRepository reservaRepository;

    private final ReservaEstadoRepository reservaEstadoRepository;

    private final ReservaUsuarioRepository reservaUsuarioRepository;

    private final ValidacionIngresoRepository validacionIngresoRepository;

    private final ControlOcupacionRepository controlOcupacionRepository;

    private final OcupacionEstadoRepository ocupacionEstadoRepository;


    public ReservaConsultaService(
            ReservaRepository reservaRepository,
            ReservaEstadoRepository reservaEstadoRepository,
            ReservaUsuarioRepository reservaUsuarioRepository,
            ValidacionIngresoRepository validacionIngresoRepository,
            ControlOcupacionRepository controlOcupacionRepository,
            OcupacionEstadoRepository ocupacionEstadoRepository) {

        this.reservaRepository =
                reservaRepository;

        this.reservaEstadoRepository =
                reservaEstadoRepository;

        this.reservaUsuarioRepository =
                reservaUsuarioRepository;

        this.validacionIngresoRepository =
                validacionIngresoRepository;

        this.controlOcupacionRepository =
                controlOcupacionRepository;

        this.ocupacionEstadoRepository =
                ocupacionEstadoRepository;
    }


    @Transactional(readOnly = true)
    public List<ReservaGrillaResponse> obtenerReservas(
            Integer idUbicacion,
            LocalDate fecha) {

        LocalDateTime inicioDia =
                fecha.atStartOfDay();

        LocalDateTime finDia =
                fecha
                        .plusDays(1)
                        .atStartOfDay();


        return reservaRepository
                .buscarPorUbicacionYFecha(
                        idUbicacion,
                        inicioDia,
                        finDia
                )
                .stream()
                .filter(
                        this::reservaVigente
                )
                .map(
                        this::convertirRespuesta
                )
                .toList();
    }


    private boolean reservaVigente(
            Reserva reserva) {

        return reservaEstadoRepository
                .findTopByReservaOrderByFechaHoraEstadoDesc(
                        reserva
                )
                .map(estado ->
                        estado.getEstadoReserva()
                                == EstadoReserva.VIGENTE
                )
                .orElse(false);
    }


    private ReservaGrillaResponse convertirRespuesta(
            Reserva reserva) {

        List<ReservaUsuario> integrantes =
                reservaUsuarioRepository
                        .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(
                                reserva
                        );


        String responsable =
                integrantes.stream()
                        .filter(reservaUsuario ->
                                reservaUsuario.getRolEnReserva()
                                        == RolEnReserva.RESPONSABLE
                        )
                        .map(
                                ReservaUsuario::getUsuario
                        )
                        .map(usuario ->
                                usuario.getNombres()
                                        + " "
                                        + usuario.getApellidos()
                        )
                        .findFirst()
                        .orElse(
                                "—"
                        );


        List<PeriodoOcupacionResponse> periodosOcupados =
                obtenerPeriodosOcupados(
                        reserva
                );


        return new ReservaGrillaResponse(
                reserva.getIdReserva(),
                reserva.getAmbiente()
                        .getIdAmbiente(),
                reserva.getCodigoReserva(),
                reserva.getFechaHoraInicio(),
                reserva.getFechaHoraFin(),
                reserva.getFechaCreacion(),
                responsable,
                periodosOcupados
        );
    }


    private List<PeriodoOcupacionResponse> obtenerPeriodosOcupados(
            Reserva reserva) {

        ControlOcupacion controlOcupacion =
                controlOcupacionRepository
                        .findByReserva(
                                reserva
                        )
                        .orElse(
                                null
                        );


        if (controlOcupacion == null) {

            return List.of();
        }


        List<OcupacionEstado> historial =
                ocupacionEstadoRepository
                        .findByControlOcupacionOrderByFechaHoraEstadoAsc(
                                controlOcupacion
                        );


        Map<String, OcupacionEstado> ultimoEstadoPorPeriodo =
                new HashMap<>();


        for (OcupacionEstado estado : historial) {

            String clave =
                    estado.getFechaHoraInicioPeriodo()
                            + "|"
                            + estado.getFechaHoraFinPeriodo();


            ultimoEstadoPorPeriodo.put(
                    clave,
                    estado
            );
        }


        List<PeriodoOcupacionResponse> periodosOcupados =
                new ArrayList<>();


        for (
                OcupacionEstado estado
                : ultimoEstadoPorPeriodo.values()
        ) {

            if (
                    estado.getEstadoOcupacion()
                            != EstadoOcupacion.OCUPADO
            ) {

                continue;
            }


            periodosOcupados.add(
                    new PeriodoOcupacionResponse(
                            estado.getFechaHoraInicioPeriodo(),
                            estado.getFechaHoraFinPeriodo()
                    )
            );
        }


        periodosOcupados.sort(
                (periodo1, periodo2) ->
                        periodo1
                                .fechaHoraInicio()
                                .compareTo(
                                        periodo2.fechaHoraInicio()
                                )
        );


        return periodosOcupados;
    }


    @Transactional(readOnly = true)
    public DetalleReservaResponse obtenerDetalleReserva(
            Integer idReserva) {

        Reserva reserva =
                reservaRepository
                        .findById(
                                idReserva
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la reserva."
                                )
                        );


        List<ReservaUsuario> integrantes =
                reservaUsuarioRepository
                        .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(
                                reserva
                        );


        List<IntegranteReservaResponse> integrantesResponse =
                integrantes.stream()
                        .map(reservaUsuario -> {

                            Usuario usuario =
                                    reservaUsuario.getUsuario();


                            boolean validado =
                                    validacionIngresoRepository
                                            .existsByReservaUsuario(
                                                    reservaUsuario
                                            );


                            return new IntegranteReservaResponse(
                                    reservaUsuario.getIdReservaUsuario(),
                                    usuario.getCodigoUniversitario(),
                                    usuario.getDni(),
                                    usuario.getNombres(),
                                    usuario.getApellidos(),
                                    reservaUsuario.getRolEnReserva(),
                                    validado
                            );
                        })
                        .toList();


        return new DetalleReservaResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                reserva.getAmbiente()
                        .getCodigo(),
                reserva.getAmbiente()
                        .getNombre(),
                reserva.getFechaHoraInicio(),
                reserva.getFechaHoraFin(),
                reserva.getFechaCreacion(),
                integrantesResponse
        );
    }
}