package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.model.enums.RolEnReserva;
import com.camila.moduloautomatizado.model.enums.TipoCorreo;

import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ControlOcupacionOrquestadorService {

    private final ControlOcupacionService controlOcupacionService;

    private final CorreoElectronicoService correoElectronicoService;

    private final ReservaUsuarioRepository reservaUsuarioRepository;


    public ControlOcupacionOrquestadorService(
            ControlOcupacionService controlOcupacionService,
            CorreoElectronicoService correoElectronicoService,
            ReservaUsuarioRepository reservaUsuarioRepository) {

        this.controlOcupacionService = controlOcupacionService;
        this.correoElectronicoService = correoElectronicoService;
        this.reservaUsuarioRepository = reservaUsuarioRepository;
    }

    @Transactional
    public Optional<OcupacionEstado> procesarReserva(
            Reserva reserva,
            LocalDateTime momento) {

        Optional<OcupacionEstado> resultado = controlOcupacionService.evaluarReserva(reserva,momento);

        if (resultado.isEmpty()) {
            return resultado;
        }

        EstadoOcupacion estado = resultado.get().getEstadoOcupacion();

        if (estado == EstadoOcupacion.PENDIENTE) {
            return resultado;
        }

        Optional<TipoCorreo> tipoCorreo = obtenerTipoCorreo(estado);

        if (tipoCorreo.isEmpty()) {
            return resultado;
        }

        Usuario responsable = obtenerResponsable(reserva);

        correoElectronicoService.registrarSiNoExiste(
                reserva,
                responsable,
                tipoCorreo.get(),
                momento
        );

        return resultado;
    }

    private Optional<TipoCorreo> obtenerTipoCorreo(
            EstadoOcupacion estado) {

        if (estado == EstadoOcupacion.OCUPADO) {
            return Optional.of(TipoCorreo.OCUPACION_CONFIRMADA);
        }

        if (estado == EstadoOcupacion.LIBERADO) {
            return Optional.of(TipoCorreo.LIBERACION_AUTOMATICA);
        }

        return Optional.empty();
    }


    private Usuario obtenerResponsable(
            Reserva reserva) {

        return reservaUsuarioRepository
                .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(reserva)
                .stream()
                .filter(reservaUsuario ->
                        reservaUsuario.getRolEnReserva() == RolEnReserva.RESPONSABLE
                )
                .map(ReservaUsuario::getUsuario)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "La reserva no tiene un usuario responsable activo."
                        )
                );
    }
}