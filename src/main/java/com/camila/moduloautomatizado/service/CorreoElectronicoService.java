package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.model.entity.CorreoElectronico;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.enums.TipoCorreo;
import com.camila.moduloautomatizado.repository.CorreoElectronicoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CorreoElectronicoService {

    private final CorreoElectronicoRepository correoElectronicoRepository;
    private final EnvioCorreoService envioCorreoService;

    public CorreoElectronicoService(
            CorreoElectronicoRepository correoElectronicoRepository,
            EnvioCorreoService envioCorreoService) {

        this.correoElectronicoRepository = correoElectronicoRepository;
        this.envioCorreoService = envioCorreoService;
    }


    @Transactional
    public Optional<CorreoElectronico> registrarSiNoExiste(
            Reserva reserva,
            Usuario destinatario,
            TipoCorreo tipoCorreo,
            LocalDateTime momento) {

        if (reserva == null) {

            throw new IllegalArgumentException(
                    "La reserva es obligatoria."
            );
        }

        if (destinatario == null) {

            throw new IllegalArgumentException(
                    "El destinatario es obligatorio."
            );
        }

        if (tipoCorreo == null) {

            throw new IllegalArgumentException(
                    "El tipo de correo es obligatorio."
            );
        }

        if (momento == null) {

            throw new IllegalArgumentException(
                    "El momento de envío es obligatorio."
            );
        }

        boolean correoYaRegistrado =
                correoElectronicoRepository
                        .existsByReservaAndTipoCorreo(reserva,tipoCorreo);

        if (correoYaRegistrado) {
            return Optional.empty();
        }

        boolean correoEnviado = envioCorreoService.enviar(reserva,destinatario,tipoCorreo);

        if (!correoEnviado) {
            return Optional.empty();
        }

        CorreoElectronico correo = new CorreoElectronico();
        correo.setReserva(reserva);
        correo.setUsuarioDestinatario(destinatario);
        correo.setTipoCorreo(tipoCorreo);
        correo.setCorreoDestino(destinatario.getCorreo());
        correo.setFechaEnvio(momento);
        correo.setFechaCreacion(momento);
        correo.setUsuarioCreacion(null);

        return Optional.of(correoElectronicoRepository.save(correo));
    }
}