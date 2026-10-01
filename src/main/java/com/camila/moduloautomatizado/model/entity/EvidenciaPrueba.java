package com.camila.moduloautomatizado.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencia_prueba")
public class EvidenciaPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Integer idEvidencia;

    @Column(name = "codigo_caso",nullable = false,length = 30)
    private String codigoCaso;

    @Column(name = "fecha_hora_ejecucion",nullable = false)
    private LocalDateTime fechaHoraEjecucion;

    @Column(name = "precondiciones",nullable = false,columnDefinition = "TEXT")
    private String precondiciones;

    @Column(name = "accion",nullable = false,columnDefinition = "TEXT")
    private String accion;

    @Column(name = "resultado_esperado",nullable = false,columnDefinition = "TEXT")
    private String resultadoEsperado;

    @Column(name = "resultado_real",nullable = false,columnDefinition = "TEXT")
    private String resultadoReal;

    @Column(name = "estado",nullable = false,length = 20)
    private String estado;


    public EvidenciaPrueba() {
    }

    public Integer getIdEvidencia() {
        return idEvidencia;
    }

    public void setIdEvidencia(
            Integer idEvidencia) {

        this.idEvidencia =
                idEvidencia;
    }

    public String getCodigoCaso() {
        return codigoCaso;
    }

    public void setCodigoCaso(
            String codigoCaso) {

        this.codigoCaso = codigoCaso;
    }

    public LocalDateTime getFechaHoraEjecucion() {
        return fechaHoraEjecucion;
    }

    public void setFechaHoraEjecucion(
            LocalDateTime fechaHoraEjecucion) {

        this.fechaHoraEjecucion = fechaHoraEjecucion;
    }

    public String getPrecondiciones() {
        return precondiciones;
    }

    public void setPrecondiciones(
            String precondiciones) {

        this.precondiciones = precondiciones;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(
            String accion) {

        this.accion = accion;
    }

    public String getResultadoEsperado() {
        return resultadoEsperado;
    }

    public void setResultadoEsperado(
            String resultadoEsperado) {

        this.resultadoEsperado = resultadoEsperado;
    }

    public String getResultadoReal() {
        return resultadoReal;
    }

    public void setResultadoReal(
            String resultadoReal) {

        this.resultadoReal =  resultadoReal;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(
            String estado) {

        this.estado =  estado;
    }
}