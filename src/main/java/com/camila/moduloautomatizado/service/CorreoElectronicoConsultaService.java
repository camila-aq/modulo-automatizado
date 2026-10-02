package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.dto.CorreoElectronicoResponse;

import com.camila.moduloautomatizado.model.entity.Reserva;

import com.camila.moduloautomatizado.repository.CorreoElectronicoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CorreoElectronicoConsultaService {

    private final ReservaRepository reservaRepository;
    private final CorreoElectronicoRepository correoElectronicoRepository;

    public CorreoElectronicoConsultaService(
            ReservaRepository reservaRepository,
            CorreoElectronicoRepository correoElectronicoRepository) {

        this.reservaRepository = reservaRepository;
        this.correoElectronicoRepository = correoElectronicoRepository;
    }

    @Transactional(readOnly = true)
    public List<CorreoElectronicoResponse> obtenerPorReserva(
            Integer idReserva) {

        Reserva reserva =
                reservaRepository
                        .findById(idReserva)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la reserva."
                                )
                        );

        return correoElectronicoRepository
                .findByReservaOrderByFechaEnvioAsc(reserva)
                .stream()
                .map(correo ->
                        new CorreoElectronicoResponse(
                                correo.getIdCorreo(),
                                correo.getTipoCorreo().name(),
                                correo.getCorreoDestino(),
                                correo.getFechaEnvio()
                        )
                )
                .toList();
    }
}