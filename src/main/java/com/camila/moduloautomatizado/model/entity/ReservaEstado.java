package com.camila.moduloautomatizado.model.entity;

import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reserva_estado")
public class ReservaEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_estado")
    private Integer idReservaEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva")
    private EstadoReserva estadoReserva;

    @Column(name = "motivo", length = 150)
    private String motivo;

    @Column(name = "fecha_hora_estado")
    private LocalDateTime fechaHoraEstado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion")
    private Usuario usuarioCreacion;

    public ReservaEstado() {
    }

    public Integer getIdReservaEstado() {
        return idReservaEstado;
    }

    public void setIdReservaEstado(Integer idReservaEstado) {
        this.idReservaEstado = idReservaEstado;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public EstadoReserva getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(EstadoReserva estadoReserva) {
        this.estadoReserva = estadoReserva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaHoraEstado() {
        return fechaHoraEstado;
    }

    public void setFechaHoraEstado(LocalDateTime fechaHoraEstado) {
        this.fechaHoraEstado = fechaHoraEstado;
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