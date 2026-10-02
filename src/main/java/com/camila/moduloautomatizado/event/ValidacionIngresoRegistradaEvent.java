package com.camila.moduloautomatizado.event;

import java.time.LocalDateTime;

public class ValidacionIngresoRegistradaEvent {

    private final Integer idReserva;
    private final LocalDateTime momento;

    public ValidacionIngresoRegistradaEvent(
            Integer idReserva,
            LocalDateTime momento) {

        this.idReserva = idReserva;
        this.momento = momento;
    }

    public Integer getIdReserva() {
        return idReserva;
    }

    public LocalDateTime getMomento() {
        return momento;
    }
}