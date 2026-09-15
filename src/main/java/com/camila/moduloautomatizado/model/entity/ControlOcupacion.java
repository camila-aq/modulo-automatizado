package com.camila.moduloautomatizado.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_ocupacion")
public class ControlOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_control")
    private Integer idControl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", unique = true)
    private Reserva reserva;

    @Column(name = "fecha_hora_inicio_control")
    private LocalDateTime fechaHoraInicioControl;

    @Column(name = "fecha_hora_limite_tolerancia")
    private LocalDateTime fechaHoraLimiteTolerancia;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion")
    private Usuario usuarioCreacion;

    public ControlOcupacion() {
    }

    public Integer getIdControl() {
        return idControl;
    }

    public void setIdControl(Integer idControl) {
        this.idControl = idControl;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public LocalDateTime getFechaHoraInicioControl() {
        return fechaHoraInicioControl;
    }

    public void setFechaHoraInicioControl(LocalDateTime fechaHoraInicioControl) {
        this.fechaHoraInicioControl = fechaHoraInicioControl;
    }

    public LocalDateTime getFechaHoraLimiteTolerancia() {
        return fechaHoraLimiteTolerancia;
    }

    public void setFechaHoraLimiteTolerancia(LocalDateTime fechaHoraLimiteTolerancia) {
        this.fechaHoraLimiteTolerancia = fechaHoraLimiteTolerancia;
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