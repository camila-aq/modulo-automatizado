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
    @JoinColumn( name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "fecha_hora_inicio_control", nullable = false)
    private LocalDateTime fechaHoraInicioControl;

    @Column(name = "fecha_hora_limite_tolerancia", nullable = false)
    private LocalDateTime fechaHoraLimiteTolerancia;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion")
    private Usuario usuarioCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_modificacion")
    private Usuario usuarioModificacion;

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

    public void setFechaHoraLimiteTolerancia(
            LocalDateTime fechaHoraLimiteTolerancia) {
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

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Usuario getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(Usuario usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }
}