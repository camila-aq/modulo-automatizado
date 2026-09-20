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
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;
import com.camila.moduloautomatizado.dto.DetalleReservaManualResponse;
import com.camila.moduloautomatizado.dto.IntegranteReservaResponse;

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

    @Transactional(readOnly = true)
    public ReservaUsuario buscarReservaVigenteParaValidacionManual(
            Usuario usuario) {

        List<ReservaUsuario> asociaciones =
                reservaUsuarioRepository.findByUsuarioAndActivoTrue(
                        usuario
                );

        if (asociaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no se encuentra asociado a ninguna reserva."
            );
        }

        LocalDateTime momento = LocalDateTime.now();

        return asociaciones.stream()
                .filter(reservaUsuario ->
                        reservaEstaVigente(
                                reservaUsuario,
                                momento
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario no se encuentra asociado a una reserva vigente."
                        )
                );
    }

    @Transactional(readOnly = true)
    public DetalleReservaManualResponse obtenerDetalleReservaManual(
            ReservaUsuario reservaUsuarioBuscado) {

        Reserva reserva =
                reservaUsuarioBuscado.getReserva();

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

        return new DetalleReservaManualResponse(
                reservaUsuarioBuscado.getIdReservaUsuario(),
                reserva.getIdReserva(),
                reserva.getAmbiente()
                        .getUbicacion()
                        .getIdUbicacion(),
                reserva.getAmbiente()
                        .getIdAmbiente(),
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



    @Transactional(readOnly = true)
    public Usuario identificarUsuarioPorDni(String dni) {

        return obtenerUsuarioPorDni(dni);
    }

    @Transactional(readOnly = true)
    public Usuario identificarUsuarioPorCodigoUniversitario(
            String codigoUniversitario) {

        return usuarioRepository
                .findByCodigoUniversitario(codigoUniversitario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El código universitario no corresponde a un usuario registrado."
                        )
                );
    }

    @Transactional
    public ValidacionIngreso confirmarValidacionManual(
            Integer idReservaUsuario,
            TipoIdentificador tipoIdentificador) {

        ReservaUsuario reservaUsuario =
                reservaUsuarioRepository
                        .findById(idReservaUsuario)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la asociación del usuario con la reserva."
                                )
                        );

        return confirmarValidacionManual(
                reservaUsuario,
                tipoIdentificador
        );
    }

    @Transactional
    public ValidacionIngreso confirmarValidacionManual(
            ReservaUsuario reservaUsuario,
            TipoIdentificador tipoIdentificador) {

        LocalDateTime momento = LocalDateTime.now();

        /*
         * Se vuelve a comprobar la vigencia al confirmar.
         * La reserva pudo haber terminado o cambiado de estado
         * después de haber sido localizada.
         */
        if (!reservaEstaVigente(reservaUsuario, momento)) {
            throw new IllegalArgumentException(
                    "El usuario no se encuentra asociado a una reserva vigente."
            );
        }

        /*
         * Evita registrar más de una validación aceptada
         * para el mismo integrante de la misma reserva.
         */
        if (validacionIngresoRepository.existsByReservaUsuario(reservaUsuario)) {
            throw new IllegalArgumentException(
                    "El usuario ya cuenta con una validación aceptada para esta reserva."
            );
        }

        /*
         * En el flujo manual el administrador no selecciona
         * un punto de validación. El sistema obtiene automáticamente
         * el punto activo correspondiente al ambiente reservado.
         */
        PuntoValidacion puntoValidacion =
                puntoValidacionRepository
                        .findByAmbienteAndActivoTrue(
                                reservaUsuario
                                        .getReserva()
                                        .getAmbiente()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró un punto de validación activo para el ambiente de la reserva."
                                )
                        );

        ValidacionIngreso validacionIngreso =
                new ValidacionIngreso();

        validacionIngreso.setPuntoValidacion(
                puntoValidacion
        );

        validacionIngreso.setReservaUsuario(
                reservaUsuario
        );

        validacionIngreso.setMedioValidacion(
                MedioValidacion.INGRESO_MANUAL
        );

        validacionIngreso.setTipoIdentificador(
                tipoIdentificador
        );

        validacionIngreso.setFechaHoraValidacion(
                momento
        );

        validacionIngreso.setFechaCreacion(
                momento
        );

        return validacionIngresoRepository.save(
                validacionIngreso
        );
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