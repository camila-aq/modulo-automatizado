package com.camila.moduloautomatizado.model.entity;

import com.camila.moduloautomatizado.model.enums.TipoCorreo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "correo_electronico")
public class CorreoElectronico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_correo")
    private Integer idCorreo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_destinatario")
    private Usuario usuarioDestinatario;

    @Column(name = "correo_destino", length = 150)
    private String correoDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_correo")
    private TipoCorreo tipoCorreo;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion")
    private Usuario usuarioCreacion;

    public CorreoElectronico() {
    }

    public Integer getIdCorreo() {
        return idCorreo;
    }

    public void setIdCorreo(Integer idCorreo) {
        this.idCorreo = idCorreo;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Usuario getUsuarioDestinatario() {
        return usuarioDestinatario;
    }

    public void setUsuarioDestinatario(Usuario usuarioDestinatario) {
        this.usuarioDestinatario = usuarioDestinatario;
    }

    public String getCorreoDestino() {
        return correoDestino;
    }

    public void setCorreoDestino(String correoDestino) {
        this.correoDestino = correoDestino;
    }

    public TipoCorreo getTipoCorreo() {
        return tipoCorreo;
    }

    public void setTipoCorreo(TipoCorreo tipoCorreo) {
        this.tipoCorreo = tipoCorreo;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(Usuario usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }
}