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
    @JoinColumn(name = "id_control")
    private ControlOcupacion controlOcupacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ocupacion")
    private EstadoOcupacion estadoOcupacion;

    @Column(name = "motivo", length = 150)
    private String motivo;

    @Column(name = "fecha_hora_estado")
    private LocalDateTime fechaHoraEstado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion")
    private Usuario usuarioCreacion;

    public OcupacionEstado() {
    }

    public Integer getIdOcupacionEstado() {
        return idOcupacionEstado;
    }

    public void setIdOcupacionEstado(Integer idOcupacionEstado) {
        this.idOcupacionEstado = idOcupacionEstado;
    }

    public ControlOcupacion getControlOcupacion() {
        return controlOcupacion;
    }

    public void setControlOcupacion(ControlOcupacion controlOcupacion) {
        this.controlOcupacion = controlOcupacion;
    }

    public EstadoOcupacion getEstadoOcupacion() {
        return estadoOcupacion;
    }

    public void setEstadoOcupacion(EstadoOcupacion estadoOcupacion) {
        this.estadoOcupacion = estadoOcupacion;
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