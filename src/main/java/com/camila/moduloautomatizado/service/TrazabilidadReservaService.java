package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.dto.EventoTrazabilidadResponse;
import com.camila.moduloautomatizado.dto.ReservaTrazabilidadResumenResponse;
import com.camila.moduloautomatizado.dto.TrazabilidadReservaResponse;
import com.camila.moduloautomatizado.dto.IntegranteReservaResponse;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.CorreoElectronicoRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TrazabilidadReservaService {

    private final ReservaRepository reservaRepository;
    private final ReservaUsuarioRepository reservaUsuarioRepository;
    private final ValidacionIngresoRepository validacionIngresoRepository;
    private final ControlOcupacionRepository controlOcupacionRepository;
    private final OcupacionEstadoRepository ocupacionEstadoRepository;
    private final CorreoElectronicoRepository correoElectronicoRepository;


    public TrazabilidadReservaService(
            ReservaRepository reservaRepository,
            ReservaUsuarioRepository reservaUsuarioRepository,
            ValidacionIngresoRepository validacionIngresoRepository,
            ControlOcupacionRepository controlOcupacionRepository,
            OcupacionEstadoRepository ocupacionEstadoRepository,
            CorreoElectronicoRepository correoElectronicoRepository) {

        this.reservaRepository =
                reservaRepository;

        this.reservaUsuarioRepository =
                reservaUsuarioRepository;

        this.validacionIngresoRepository =
                validacionIngresoRepository;

        this.controlOcupacionRepository =
                controlOcupacionRepository;

        this.ocupacionEstadoRepository =
                ocupacionEstadoRepository;

        this.correoElectronicoRepository =
                correoElectronicoRepository;
    }


    @Transactional(readOnly = true)
    public List<ReservaTrazabilidadResumenResponse> buscarReservas(
            String estudiante,
            String codigoUniversitario,
            String dni,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer idUbicacion,
            Integer idAmbiente,
            String estado) {

        String estudianteBusqueda =
                normalizarTexto(
                        estudiante
                );

        String codigoUniversitarioBusqueda =
                normalizarTexto(
                        codigoUniversitario
                );

        String dniBusqueda =
                normalizarTexto(
                        dni
                );

        String estadoBusqueda =
                normalizarTexto(
                        estado
                );


        if (
                fechaDesde != null
                        &&
                        fechaHasta != null
                        &&
                        fechaDesde.isAfter(fechaHasta)
        ) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }


        return reservaRepository
                .findAll()
                .stream()
                .filter(reserva ->
                        coincideFecha(
                                reserva,
                                fechaDesde,
                                fechaHasta
                        )
                )
                .filter(reserva ->
                        coincideUbicacion(
                                reserva,
                                idUbicacion
                        )
                )
                .filter(reserva ->
                        coincideAmbiente(
                                reserva,
                                idAmbiente
                        )
                )
                .filter(reserva ->
                        coincideUsuario(
                                reserva,
                                estudianteBusqueda,
                                codigoUniversitarioBusqueda,
                                dniBusqueda
                        )
                )
                .map(this::crearResumen)
                .filter(resumen ->
                        resumen != null
                )
                .filter(resumen ->
                        estadoBusqueda == null
                                ||
                                resumen
                                        .estadoActual()
                                        .equalsIgnoreCase(
                                                estadoBusqueda
                                        )
                )
                .sorted(
                        Comparator.comparing(
                                ReservaTrazabilidadResumenResponse::fechaHoraInicio
                        ).reversed()
                )
                .toList();
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

        List<EventoTrazabilidadResponse> eventos =
                new ArrayList<>();

        agregarValidaciones(
                reserva,
                eventos
        );


        Optional<ControlOcupacion> control =
                controlOcupacionRepository
                        .findByReserva(
                                reserva
                        );

        String estadoActual =
                null;

        LocalDateTime limiteTolerancia =
                null;


        if (control.isPresent()) {

            ControlOcupacion controlOcupacion =
                    control.get();

            limiteTolerancia =
                    controlOcupacion
                            .getFechaHoraLimiteTolerancia();

            List<OcupacionEstado> estados =
                    ocupacionEstadoRepository
                            .findByControlOcupacionOrderByFechaHoraEstadoAsc(
                                    controlOcupacion
                            )
                            .stream()
                            .filter(estado ->
                                    reserva
                                            .getFechaHoraInicio()
                                            .equals(
                                                    estado
                                                            .getFechaHoraInicioPeriodo()
                                            )
                                            &&
                                            reserva
                                                    .getFechaHoraFin()
                                                    .equals(
                                                            estado
                                                                    .getFechaHoraFinPeriodo()
                                                    )
                            )
                            .toList();


            for (OcupacionEstado estado : estados) {

                eventos.add(
                        new EventoTrazabilidadResponse(
                                "ESTADO_OCUPACION",
                                estado.getFechaHoraEstado(),
                                estado
                                        .getEstadoOcupacion()
                                        .name()
                                        + " - "
                                        + estado.getMotivo()
                        )
                );
            }


            if (!estados.isEmpty()) {

                estadoActual =
                        estados
                                .get(
                                        estados.size() - 1
                                )
                                .getEstadoOcupacion()
                                .name();
            }
        }


        agregarCorreos(
                reserva,
                eventos
        );


        eventos.sort(
                Comparator.comparing(
                        EventoTrazabilidadResponse::fechaHora
                )
        );

        List<IntegranteReservaResponse> integrantes =
                obtenerIntegrantes(
                        reserva
                );

        return new TrazabilidadReservaResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                estadoActual,
                limiteTolerancia,
                integrantes,
                eventos
        );
    }


    private boolean coincideFecha(
            Reserva reserva,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        LocalDate fechaReserva =
                reserva
                        .getFechaHoraInicio()
                        .toLocalDate();


        if (
                fechaDesde != null
                        &&
                        fechaReserva.isBefore(
                                fechaDesde
                        )
        ) {

            return false;
        }


        if (
                fechaHasta != null
                        &&
                        fechaReserva.isAfter(
                                fechaHasta
                        )
        ) {

            return false;
        }


        return true;
    }


    private boolean coincideUbicacion(
            Reserva reserva,
            Integer idUbicacion) {

        if (idUbicacion == null) {
            return true;
        }


        return reserva
                .getAmbiente()
                .getUbicacion()
                .getIdUbicacion()
                .equals(
                        idUbicacion
                );
    }


    private boolean coincideAmbiente(
            Reserva reserva,
            Integer idAmbiente) {

        if (idAmbiente == null) {
            return true;
        }


        return reserva
                .getAmbiente()
                .getIdAmbiente()
                .equals(
                        idAmbiente
                );
    }


    private boolean coincideUsuario(
            Reserva reserva,
            String estudiante,
            String codigoUniversitario,
            String dni) {

        if (
                estudiante == null
                        &&
                        codigoUniversitario == null
                        &&
                        dni == null
        ) {

            return true;
        }


        List<ReservaUsuario> integrantes =
                reservaUsuarioRepository
                        .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(
                                reserva
                        );


        return integrantes
                .stream()
                .anyMatch(reservaUsuario -> {

                    Usuario usuario =
                            reservaUsuario
                                    .getUsuario();


                    String nombreCompleto =
                            usuario.getNombres()
                                    + " "
                                    + usuario.getApellidos();


                    boolean coincideEstudiante =
                            estudiante == null
                                    ||
                                    nombreCompleto
                                            .toUpperCase(
                                                    Locale.ROOT
                                            )
                                            .contains(
                                                    estudiante
                                                            .toUpperCase(
                                                                    Locale.ROOT
                                                            )
                                            );


                    boolean coincideCodigo =
                            codigoUniversitario == null
                                    ||
                                    usuario
                                            .getCodigoUniversitario()
                                            .equalsIgnoreCase(
                                                    codigoUniversitario
                                            );


                    boolean coincideDni =
                            dni == null
                                    ||
                                    usuario
                                            .getDni()
                                            .equals(
                                                    dni
                                            );


                    return coincideEstudiante
                            &&
                            coincideCodigo
                            &&
                            coincideDni;
                });
    }
    private ReservaTrazabilidadResumenResponse crearResumen(
            Reserva reserva) {

        String estadoActual =
                obtenerEstadoActual(
                        reserva
                );


        if (estadoActual == null) {
            return null;
        }


        return new ReservaTrazabilidadResumenResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                reserva.getFechaHoraInicio(),
                reserva.getFechaHoraFin(),
                reserva
                        .getAmbiente()
                        .getUbicacion()
                        .getIdUbicacion(),
                reserva
                        .getAmbiente()
                        .getUbicacion()
                        .getNombre(),
                reserva
                        .getAmbiente()
                        .getNombre(),
                reserva
                        .getAmbiente()
                        .getPiso(),
                estadoActual
        );
    }


    private String obtenerEstadoActual(
            Reserva reserva) {

        Optional<ControlOcupacion> control =
                controlOcupacionRepository
                        .findByReserva(
                                reserva
                        );


        if (control.isEmpty()) {
            return null;
        }


        return ocupacionEstadoRepository
                .findTopByControlOcupacionAndFechaHoraInicioPeriodoAndFechaHoraFinPeriodoOrderByFechaHoraEstadoDesc(
                        control.get(),
                        reserva.getFechaHoraInicio(),
                        reserva.getFechaHoraFin()
                )
                .map(estado ->
                        estado
                                .getEstadoOcupacion()
                                .name()
                )
                .orElse(null);
    }


    private void agregarValidaciones(
            Reserva reserva,
            List<EventoTrazabilidadResponse> eventos) {

        List<ValidacionIngreso> validaciones =
                validacionIngresoRepository
                        .findValidacionesIngresoPorReserva(
                                reserva
                        );


        for (ValidacionIngreso validacion : validaciones) {

            Usuario usuario =
                    validacion
                            .getReservaUsuario()
                            .getUsuario();


            String detalle =
                    usuario.getCodigoUniversitario()
                            + " - "
                            + usuario
                            .getNombres()
                            .toUpperCase(
                                    Locale.ROOT
                            )
                            + " "
                            + usuario
                            .getApellidos()
                            .toUpperCase(
                                    Locale.ROOT
                            );


            eventos.add(
                    new EventoTrazabilidadResponse(
                            "VALIDACION_INGRESO",
                            validacion
                                    .getFechaHoraValidacion(),
                            detalle
                    )
            );
        }
    }


    private void agregarCorreos(
            Reserva reserva,
            List<EventoTrazabilidadResponse> eventos) {

        correoElectronicoRepository
                .findByReservaOrderByFechaEnvioAsc(
                        reserva
                )
                .forEach(correo ->
                        eventos.add(
                                new EventoTrazabilidadResponse(
                                        "CORREO_ELECTRONICO",
                                        correo.getFechaEnvio(),
                                        correo
                                                .getTipoCorreo()
                                                .name()
                                )
                        )
                );
    }


    private String normalizarTexto(
            String valor) {

        if (
                valor == null
                        ||
                        valor.isBlank()
        ) {

            return null;
        }


        return valor.trim();
    }

    private List<IntegranteReservaResponse> obtenerIntegrantes(
            Reserva reserva) {

        List<Integer> usuariosValidados =
                validacionIngresoRepository
                        .findValidacionesIngresoPorReserva(
                                reserva
                        )
                        .stream()
                        .map(validacion ->
                                validacion
                                        .getReservaUsuario()
                                        .getUsuario()
                                        .getIdUsuario()
                        )
                        .distinct()
                        .toList();


        return reservaUsuarioRepository
                .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(
                        reserva
                )
                .stream()
                .map(reservaUsuario -> {

                    Usuario usuario =
                            reservaUsuario
                                    .getUsuario();


                    boolean validado =
                            usuariosValidados.contains(
                                    usuario.getIdUsuario()
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
    }
}