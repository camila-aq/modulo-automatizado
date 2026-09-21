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

import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;

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
import java.util.List;

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


        /*
         * La reserva debe continuar vigente.
         * Esto evita ocupar reservas canceladas
         * o finalizadas.
         */
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


        LocalDateTime momento =
                LocalDateTime.now();


        /*
         * La ocupación manual puede realizarse
         * desde los minutos de anticipación
         * permitidos hasta antes de la hora fin.
         *
         * No se aplica la tolerancia automática.
         */
        LocalDateTime inicioPermitido =
                reserva.getFechaHoraInicio()
                        .minusMinutes(
                                ReglasControlOcupacion
                                        .MINUTOS_ANTICIPACION
                        );

        boolean dentroDelHorario =
                !momento.isBefore(
                        inicioPermitido
                )
                        &&
                        momento.isBefore(
                                reserva.getFechaHoraFin()
                        );

        if (!dentroDelHorario) {

            throw new IllegalArgumentException(
                    "La reserva no se encuentra dentro del horario permitido para ocupar."
            );
        }


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


        ControlOcupacion controlOcupacion =
                obtenerOCrearControlOcupacion(
                        reserva,
                        momento
                );


        boolean yaOcupada =
                ocupacionEstadoRepository
                        .findTopByControlOcupacionOrderByFechaHoraEstadoDesc(
                                controlOcupacion
                        )
                        .map(estado ->
                                estado.getEstadoOcupacion()
                                        == EstadoOcupacion.OCUPADO
                        )
                        .orElse(false);

        if (yaOcupada) {

            throw new IllegalArgumentException(
                    "La reserva ya se encuentra ocupada."
            );
        }


        int validacionesRegistradas =
                0;


        /*
         * Ocupar manualmente la reserva implica
         * validar a todos sus integrantes.
         *
         * Si alguno ya había validado su ingreso
         * individualmente, se conserva esa
         * validación y no se duplica.
         */
        for (ReservaUsuario integrante : integrantes) {

            if (
                    validacionIngresoRepository
                            .existsByReservaUsuario(
                                    integrante
                            )
            ) {
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


        OcupacionEstado estadoOcupado =
                new OcupacionEstado();

        estadoOcupado.setControlOcupacion(
                controlOcupacion
        );

        estadoOcupado.setEstadoOcupacion(
                EstadoOcupacion.OCUPADO
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
                integrantes.size(),
                validacionesRegistradas,
                momento
        );
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
                     * Estos valores representan
                     * la configuración temporal del
                     * control, pero NO se evalúan
                     * para decidir la ocupación manual.
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