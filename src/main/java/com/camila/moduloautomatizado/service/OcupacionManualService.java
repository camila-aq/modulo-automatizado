package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.dto.OcuparReservaResponse;

import com.camila.moduloautomatizado.model.entity.ControlOcupacion;
import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;

import com.camila.moduloautomatizado.repository.ControlOcupacionRepository;
import com.camila.moduloautomatizado.repository.OcupacionEstadoRepository;
import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.ValidacionIngresoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OcupacionManualService {

    private final ReservaRepository reservaRepository;

    private final ReservaEstadoRepository reservaEstadoRepository;

    private final ReservaUsuarioRepository reservaUsuarioRepository;

    private final ValidacionIngresoRepository validacionIngresoRepository;

    private final PuntoValidacionRepository puntoValidacionRepository;

    private final ControlOcupacionRepository controlOcupacionRepository;

    private final OcupacionEstadoRepository ocupacionEstadoRepository;


    public OcupacionManualService(
            ReservaRepository reservaRepository,
            ReservaEstadoRepository reservaEstadoRepository,
            ReservaUsuarioRepository reservaUsuarioRepository,
            ValidacionIngresoRepository validacionIngresoRepository,
            PuntoValidacionRepository puntoValidacionRepository,
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

        this.puntoValidacionRepository =
                puntoValidacionRepository;

        this.controlOcupacionRepository =
                controlOcupacionRepository;

        this.ocupacionEstadoRepository =
                ocupacionEstadoRepository;
    }


    @Transactional
    public OcuparReservaResponse ocuparReserva(
            Integer idReserva,
            LocalDateTime fechaHoraInicioPeriodo) {

        if (fechaHoraInicioPeriodo == null) {

            throw new IllegalArgumentException(
                    "Debe indicar la hora que se desea ocupar."
            );
        }


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


        validarReservaVigente(
                reserva
        );


        LocalDateTime fechaHoraFinPeriodo =
                fechaHoraInicioPeriodo
                        .plusHours(1);


        validarPeriodoDentroDeReserva(
                reserva,
                fechaHoraInicioPeriodo,
                fechaHoraFinPeriodo
        );


        List<ReservaUsuario> integrantes =
                reservaUsuarioRepository
                        .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(
                                reserva
                        );

        if (integrantes.isEmpty()) {

            throw new IllegalArgumentException(
                    "La reserva no tiene integrantes activos."
            );
        }


        PuntoValidacion puntoValidacion =
                puntoValidacionRepository
                        .findByAmbienteAndActivoTrue(
                                reserva.getAmbiente()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró un punto de validación activo para el ambiente."
                                )
                        );


        LocalDateTime momento =
                LocalDateTime.now();


        ControlOcupacion controlOcupacion =
                obtenerOCrearControlOcupacion(
                        reserva,
                        momento
                );


        if (
                periodoYaOcupado(
                        controlOcupacion,
                        fechaHoraInicioPeriodo,
                        fechaHoraFinPeriodo
                )
        ) {

            throw new IllegalArgumentException(
                    "La hora seleccionada ya se encuentra ocupada."
            );
        }


        int validacionesRegistradas =
                registrarValidacionesPendientes(
                        integrantes,
                        puntoValidacion,
                        momento
                );


        OcupacionEstado estadoOcupado =
                new OcupacionEstado();

        estadoOcupado.setControlOcupacion(
                controlOcupacion
        );

        estadoOcupado.setEstadoOcupacion(
                EstadoOcupacion.OCUPADO
        );

        estadoOcupado.setFechaHoraInicioPeriodo(
                fechaHoraInicioPeriodo
        );

        estadoOcupado.setFechaHoraFinPeriodo(
                fechaHoraFinPeriodo
        );

        estadoOcupado.setMotivo(
                "Ocupación manual registrada por el administrador"
        );

        estadoOcupado.setFechaHoraEstado(
                momento
        );

        estadoOcupado.setFechaCreacion(
                momento
        );

        estadoOcupado.setUsuarioCreacion(
                null
        );


        ocupacionEstadoRepository.save(
                estadoOcupado
        );


        return new OcuparReservaResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                EstadoOcupacion.OCUPADO.name(),
                fechaHoraInicioPeriodo,
                fechaHoraFinPeriodo,
                integrantes.size(),
                validacionesRegistradas,
                momento
        );
    }


    private void validarReservaVigente(
            Reserva reserva) {

        boolean reservaVigente =
                reservaEstadoRepository
                        .findTopByReservaOrderByFechaHoraEstadoDesc(
                                reserva
                        )
                        .map(estado ->
                                estado.getEstadoReserva()
                                        == EstadoReserva.VIGENTE
                        )
                        .orElse(false);


        if (!reservaVigente) {

            throw new IllegalArgumentException(
                    "La reserva no se encuentra vigente."
            );
        }
    }


    private void validarPeriodoDentroDeReserva(
            Reserva reserva,
            LocalDateTime inicioPeriodo,
            LocalDateTime finPeriodo) {

        /*
         * La fila debe representar una hora
         * exacta de la grilla.
         */
        boolean horaExacta =
                inicioPeriodo.getMinute() == 0
                        &&
                        inicioPeriodo.getSecond() == 0
                        &&
                        inicioPeriodo.getNano() == 0;

        if (!horaExacta) {

            throw new IllegalArgumentException(
                    "La hora seleccionada no corresponde a una franja válida."
            );
        }

        boolean inicioValido =
                !inicioPeriodo.isBefore(
                        reserva.getFechaHoraInicio()
                );

        boolean finValido =
                !finPeriodo.isAfter(
                        reserva.getFechaHoraFin()
                );


        if (
                !inicioValido
                        ||
                        !finValido
        ) {

            throw new IllegalArgumentException(
                    "La hora seleccionada no pertenece al bloque de la reserva."
            );
        }
    }


    private boolean periodoYaOcupado(
            ControlOcupacion controlOcupacion,
            LocalDateTime inicioPeriodo,
            LocalDateTime finPeriodo) {

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

        return ultimoEstadoPorPeriodo
                .values()
                .stream()
                .filter(estado ->
                        estado.getEstadoOcupacion()
                                == EstadoOcupacion.OCUPADO
                )
                .anyMatch(estado -> {

                    LocalDateTime inicioExistente =
                            estado.getFechaHoraInicioPeriodo();

                    LocalDateTime finExistente =
                            estado.getFechaHoraFinPeriodo();

                    return (
                            inicioExistente.isBefore(
                                    finPeriodo
                            )
                                    &&
                                    finExistente.isAfter(
                                            inicioPeriodo
                                    )
                    );
                });
    }


    private int registrarValidacionesPendientes(
            List<ReservaUsuario> integrantes,
            PuntoValidacion puntoValidacion,
            LocalDateTime momento) {

        int validacionesRegistradas =
                0;

        for (ReservaUsuario integrante : integrantes) {

            boolean yaValidado =
                    validacionIngresoRepository
                            .existsByReservaUsuario(
                                    integrante
                            );

            if (yaValidado) {
                continue;
            }


            ValidacionIngreso validacion =
                    new ValidacionIngreso();

            validacion.setPuntoValidacion(
                    puntoValidacion
            );

            validacion.setReservaUsuario(
                    integrante
            );

            validacion.setMedioValidacion(
                    MedioValidacion.OCUPACION_MANUAL
            );

            validacion.setTipoIdentificador(
                    TipoIdentificador.NO_APLICA
            );

            validacion.setFechaHoraValidacion(
                    momento
            );

            validacion.setFechaCreacion(
                    momento
            );

            validacion.setUsuarioCreacion(
                    null
            );


            validacionIngresoRepository.save(
                    validacion
            );

            validacionesRegistradas++;
        }


        return validacionesRegistradas;
    }


    private ControlOcupacion obtenerOCrearControlOcupacion(
            Reserva reserva,
            LocalDateTime momento) {

        return controlOcupacionRepository
                .findByReserva(
                        reserva
                )
                .orElseGet(() -> {

                    ControlOcupacion control =
                            new ControlOcupacion();

                    control.setReserva(
                            reserva
                    );

                    /*
                     * CONTROL_OCUPACION sigue
                     * perteneciendo a la reserva
                     * completa.
                     */
                    control.setFechaHoraInicioControl(
                            reserva.getFechaHoraInicio()
                    );

                    control.setFechaHoraLimiteTolerancia(
                            reserva.getFechaHoraInicio()
                                    .plusMinutes(
                                            reserva.getToleranciaMinutos()
                                    )
                    );

                    control.setFechaCreacion(
                            momento
                    );

                    control.setUsuarioCreacion(
                            null
                    );


                    return controlOcupacionRepository.save(
                            control
                    );
                });
    }
}