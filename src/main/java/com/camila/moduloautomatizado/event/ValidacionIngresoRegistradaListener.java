
package com.camila.moduloautomatizado.event;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.service.ControlOcupacionOrquestadorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(
        name = "app.r4-mode.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class ValidacionIngresoRegistradaListener {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ValidacionIngresoRegistradaListener.class
            );

    private final ReservaRepository reservaRepository;

    private final ControlOcupacionOrquestadorService controlOcupacionOrquestadorService;

    public ValidacionIngresoRegistradaListener(
            ReservaRepository reservaRepository,
            ControlOcupacionOrquestadorService
                    controlOcupacionOrquestadorService) {

        this.reservaRepository = reservaRepository;
        this.controlOcupacionOrquestadorService = controlOcupacionOrquestadorService;
    }

    @Async("controlOcupacionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarValidacionRegistrada(
            ValidacionIngresoRegistradaEvent evento) {

        try {

            Reserva reserva =
                    reservaRepository
                            .findById(evento.getIdReserva())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No se encontró la reserva asociada a la validación."
                                    )
                            );

            controlOcupacionOrquestadorService
                    .procesarReserva(
                            reserva,
                            evento.getMomento()
                    );

        } catch (RuntimeException ex) {

            logger.error(
                    "No se pudo ejecutar el control automático "
                            + "después de la validación "
                            + "para la reserva ID {}",
                    evento.getIdReserva(),
                    ex
            );
        }
    }
}
