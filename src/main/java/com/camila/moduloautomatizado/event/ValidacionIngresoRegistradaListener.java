package com.camila.moduloautomatizado.event;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.service.ControlOcupacionOrquestadorService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ValidacionIngresoRegistradaListener {

    private final ReservaRepository reservaRepository;

    private final ControlOcupacionOrquestadorService controlOcupacionOrquestadorService;


    public ValidacionIngresoRegistradaListener(
            ReservaRepository reservaRepository,
            ControlOcupacionOrquestadorService controlOcupacionOrquestadorService) {

        this.reservaRepository = reservaRepository;
        this.controlOcupacionOrquestadorService = controlOcupacionOrquestadorService;
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarValidacionRegistrada(ValidacionIngresoRegistradaEvent evento) {

        try {

            Reserva reserva =
                    reservaRepository
                            .findById(evento.getIdReserva())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No se encontró la reserva asociada a la validación."
                                    )
                            );

            controlOcupacionOrquestadorService.procesarReserva(reserva,evento.getMomento());

        } catch (RuntimeException ex) {

            System.err.println(
                    "No se pudo ejecutar el control automático "
                    + "después de la validación: " + ex.getMessage()
            );
        }
    }
}