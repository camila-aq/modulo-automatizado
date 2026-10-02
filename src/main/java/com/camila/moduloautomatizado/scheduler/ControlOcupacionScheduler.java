package com.camila.moduloautomatizado.scheduler;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.service.ControlOcupacionOrquestadorService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ControlOcupacionScheduler {

    private final ReservaRepository reservaRepository;

    private final ControlOcupacionOrquestadorService
            controlOcupacionOrquestadorService;


    public ControlOcupacionScheduler(
            ReservaRepository reservaRepository,
            ControlOcupacionOrquestadorService
                    controlOcupacionOrquestadorService) {

        this.reservaRepository = reservaRepository;
        this.controlOcupacionOrquestadorService = controlOcupacionOrquestadorService;
    }

    @Scheduled(
            fixedDelay = 60000,
            initialDelay = 10000
    )
    public void evaluarReservasEnControl() {

        LocalDateTime momento = LocalDateTime.now();

        List<Reserva> reservas = reservaRepository.buscarReservasEnControl(momento);

        for (Reserva reserva : reservas) {
            try {
                controlOcupacionOrquestadorService.procesarReserva(reserva,momento);
            } catch (RuntimeException ex) {
                System.err.println(
                        "No se pudo procesar la reserva " + reserva.getCodigoReserva()
                        + ": " + ex.getMessage()
                );
            }
        }
    }
}