package com.camila.moduloautomatizado.model.entity;

import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocupacion_estado")
public class OcupacionEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ocupacion_estado")
    private Integer idOcupacionEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_control",
            nullable = false
    )
    private ControlOcupacion controlOcupacion;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado_ocupacion",
            nullable = false,
            length = 20
    )
    private EstadoOcupacion estadoOcupacion;

    /*
     * Período concreto al que corresponde
     * este estado de ocupación.
     *
     * Ejemplos:
     *
     * Ocupación manual de una fila:
     * 17:00 - 18:00
     *
     * Ocupación automática del bloque:
     * 17:00 - 19:00
     */
    @Column(
            name = "fecha_hora_inicio_periodo",
            nullable = false
    )
    private LocalDateTime fechaHoraInicioPeriodo;

    @Column(
            name = "fecha_hora_fin_periodo",
            nullable = false
    )
    private LocalDateTime fechaHoraFinPeriodo;

    @Column(
            name = "motivo",
            length = 150
    )
    private String motivo;

    @Column(
            name = "fecha_hora_estado",
            nullable = false
    )
    private LocalDateTime fechaHoraEstado;

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion")
    private Usuario usuarioCreacion;


    public OcupacionEstado() {
    }


    public Integer getIdOcupacionEstado() {

        return idOcupacionEstado;
    }


    public void setIdOcupacionEstado(
            Integer idOcupacionEstado) {

        this.idOcupacionEstado =
                idOcupacionEstado;
    }


    public ControlOcupacion getControlOcupacion() {

        return controlOcupacion;
    }


    public void setControlOcupacion(
            ControlOcupacion controlOcupacion) {

        this.controlOcupacion =
                controlOcupacion;
    }


    public EstadoOcupacion getEstadoOcupacion() {

        return estadoOcupacion;
    }


    public void setEstadoOcupacion(
            EstadoOcupacion estadoOcupacion) {

        this.estadoOcupacion =
                estadoOcupacion;
    }


    public LocalDateTime getFechaHoraInicioPeriodo() {

        return fechaHoraInicioPeriodo;
    }


    public void setFechaHoraInicioPeriodo(
            LocalDateTime fechaHoraInicioPeriodo) {

        this.fechaHoraInicioPeriodo =
                fechaHoraInicioPeriodo;
    }


    public LocalDateTime getFechaHoraFinPeriodo() {

        return fechaHoraFinPeriodo;
    }


    public void setFechaHoraFinPeriodo(
            LocalDateTime fechaHoraFinPeriodo) {

        this.fechaHoraFinPeriodo =
                fechaHoraFinPeriodo;
    }


    public String getMotivo() {

        return motivo;
    }


    public void setMotivo(
            String motivo) {

        this.motivo =
                motivo;
    }


    public LocalDateTime getFechaHoraEstado() {

        return fechaHoraEstado;
    }


    public void setFechaHoraEstado(
            LocalDateTime fechaHoraEstado) {

        this.fechaHoraEstado =
                fechaHoraEstado;
    }


    public LocalDateTime getFechaCreacion() {

        return fechaCreacion;
    }


    public void setFechaCreacion(
            LocalDateTime fechaCreacion) {

        this.fechaCreacion =
                fechaCreacion;
    }


    public Usuario getUsuarioCreacion() {

        return usuarioCreacion;
    }


    public void setUsuarioCreacion(
            Usuario usuarioCreacion) {

        this.usuarioCreacion =
                usuarioCreacion;
    }
}