package com.camila.moduloautomatizado.service;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.enums.TipoCorreo;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class EnvioCorreoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final JavaMailSender javaMailSender;
    private final ReservaUsuarioRepository reservaUsuarioRepository;


    @Value("${app.mail.enabled:false}")
    private boolean envioHabilitado;

    @Value("${app.mail.from:}")
    private String correoRemitente;

    @Value("${app.mail.override-to:}")
    private String correoDestinoPrueba;


    public EnvioCorreoService(
            JavaMailSender javaMailSender,
            ReservaUsuarioRepository
                    reservaUsuarioRepository) {

        this.javaMailSender = javaMailSender;
        this.reservaUsuarioRepository = reservaUsuarioRepository;
    }


    public boolean enviar(
            Reserva reserva,
            Usuario destinatario,
            TipoCorreo tipoCorreo) {

        if (!envioHabilitado) {
            return false;
        }

        String correoDestino = obtenerCorreoDestino(destinatario);

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom(correoRemitente);
        mensaje.setTo(correoDestino);
        mensaje.setSubject(construirAsunto(reserva,tipoCorreo));
        mensaje.setText(construirContenido(reserva,tipoCorreo));

        try {

            javaMailSender.send(mensaje);
            return true;

        } catch (MailException ex) {

            System.err.println(
                    "No fue posible enviar el correo de "
                    + tipoCorreo + " para la reserva "
                    + reserva.getCodigoReserva() + ": " + ex.getMessage()
            );

            return false;
        }
    }


    private String obtenerCorreoDestino(
            Usuario destinatario) {

        if (correoDestinoPrueba != null && !correoDestinoPrueba.isBlank()) {
            return correoDestinoPrueba;
        }

        return destinatario.getCorreo();
    }


    private String construirAsunto(
            Reserva reserva,
            TipoCorreo tipoCorreo) {

        String ubicacion = reserva.getAmbiente().getUbicacion().getNombre();

        if ( tipoCorreo == TipoCorreo.OCUPACION_CONFIRMADA) {
            return "Confirmación de ocupación de reserva de Biblioteca " + ubicacion;
        }

        return "Liberación automática de reserva de Biblioteca " + ubicacion;
    }


    private String construirContenido(
            Reserva reserva,
            TipoCorreo tipoCorreo) {

        StringBuilder contenido =  new StringBuilder();

        contenido.append("Fecha de reserva:    ");
        contenido.append(reserva.getFechaHoraInicio().format(FORMATO_FECHA));
        contenido.append("\nAmbiente reservado: ");
        contenido.append(reserva.getAmbiente().getNombre());
        contenido.append(" - ");
        contenido.append(reserva.getAmbiente().getPiso());
        contenido.append("\nHoras:               ");
        contenido.append(reserva.getFechaHoraInicio().format(FORMATO_HORA));
        contenido.append(" - ");
        contenido.append(reserva.getFechaHoraFin().format(FORMATO_HORA));
        contenido.append("\n\nIntegrantes:\n");

        List<ReservaUsuario> integrantes =
                reservaUsuarioRepository
                        .findByReservaAndActivoTrueOrderByIdReservaUsuarioAsc(reserva);

        for (ReservaUsuario reservaUsuario : integrantes) {
            Usuario usuario = reservaUsuario.getUsuario();
            contenido.append(usuario.getCodigoUniversitario());
            contenido.append(" - ");
            contenido.append(usuario.getNombres().toUpperCase(Locale.ROOT));
            contenido.append(" ");
            contenido.append(usuario.getApellidos().toUpperCase(Locale.ROOT));
            contenido.append("\n");
        }

        contenido.append("\nMotivo: ");

        if (tipoCorreo == TipoCorreo.OCUPACION_CONFIRMADA) {
            contenido.append("Se alcanzó la cantidad mínima de usuarios validados " +
                    "dentro del tiempo de tolerancia.");
        } else {
            contenido.append("No se alcanzó la cantidad mínima de usuarios validados dentro " +
                    "del tiempo de tolerancia, por lo que el ambiente fue liberado automáticamente.");
        }

        return contenido.toString();
    }
}