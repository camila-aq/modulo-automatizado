package com.camila.moduloautomatizado.model.entity;

import com.camila.moduloautomatizado.model.enums.MedioValidacion;
import com.camila.moduloautomatizado.model.enums.TipoIdentificador;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "validacion_ingreso")
public class ValidacionIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validacion")
    private Integer idValidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_punto_validacion")
    private PuntoValidacion puntoValidacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva_usuario", unique = true)
    private ReservaUsuario reservaUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_validacion")
    private MedioValidacion medioValidacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_identificador")
    private TipoIdentificador tipoIdentificador;

    @Column(name = "fecha_hora_validacion")
    private LocalDateTime fechaHoraValidacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion")
    private Usuario usuarioCreacion;

    public ValidacionIngreso() {
    }

    public Integer getIdValidacion() {
        return idValidacion;
    }

    public void setIdValidacion(Integer idValidacion) {
        this.idValidacion = idValidacion;
    }

    public PuntoValidacion getPuntoValidacion() {
        return puntoValidacion;
    }

    public void setPuntoValidacion(PuntoValidacion puntoValidacion) {
        this.puntoValidacion = puntoValidacion;
    }

    public ReservaUsuario getReservaUsuario() {
        return reservaUsuario;
    }

    public void setReservaUsuario(ReservaUsuario reservaUsuario) {
        this.reservaUsuario = reservaUsuario;
    }

    public MedioValidacion getMedioValidacion() {
        return medioValidacion;
    }

    public void setMedioValidacion(MedioValidacion medioValidacion) {
        this.medioValidacion = medioValidacion;
    }

    public TipoIdentificador getTipoIdentificador() {
        return tipoIdentificador;
    }

    public void setTipoIdentificador(TipoIdentificador tipoIdentificador) {
        this.tipoIdentificador = tipoIdentificador;
    }

    public LocalDateTime getFechaHoraValidacion() {
        return fechaHoraValidacion;
    }

    public void setFechaHoraValidacion(LocalDateTime fechaHoraValidacion) {
        this.fechaHoraValidacion = fechaHoraValidacion;
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