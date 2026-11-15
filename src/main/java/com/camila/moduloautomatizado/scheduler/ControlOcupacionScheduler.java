
package com.camila.moduloautomatizado.scheduler;

import com.camila.moduloautomatizado.model.entity.OcupacionEstado;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.enums.EstadoOcupacion;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.service.ControlOcupacionOrquestadorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(
        name = "app.r4-mode.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class ControlOcupacionScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(ControlOcupacionScheduler.class);

    private final ReservaRepository reservaRepository;

    private final ControlOcupacionOrquestadorService
            controlOcupacionOrquestadorService;

    @Value("${app.r6-data.enabled:false}")
    private boolean r6Habilitado;

    @Value("${app.r6-data.scenario:OCUPACION}")
    private String escenarioR6;

    @Value("${app.r6-data.run-id:1}")
    private int numeroEjecucionR6;


    private final Set<Integer> liberacionesMedidas =
            ConcurrentHashMap.newKeySet();

    public ControlOcupacionScheduler(
            ReservaRepository reservaRepository,
            ControlOcupacionOrquestadorService
                    controlOcupacionOrquestadorService) {

        this.reservaRepository = reservaRepository;
        this.controlOcupacionOrquestadorService = controlOcupacionOrquestadorService;
    }

    @Scheduled(
            fixedDelay = 1000,
            initialDelay = 1000
    )
    public void evaluarReservasEnControl() {

        long inicioCiclo = System.nanoTime();
        LocalDateTime momento = LocalDateTime.now();

        List<Reserva> reservas = reservaRepository.buscarReservasEnControl(momento);

        boolean medirR6 = r6Habilitado && "LIBERACION".equalsIgnoreCase(escenarioR6);

        String prefijoR6 = String.format(
                Locale.ROOT,
                "R6L%03d",
                numeroEjecucionR6
        );

        int reservasR6Evaluadas = 0;
        int liberacionesR6Registradas = 0;

        for (Reserva reserva : reservas) {
            try {

                boolean perteneceAR6 = medirR6 && reserva.getCodigoReserva().startsWith(prefijoR6);

                if (perteneceAR6) {
                    reservasR6Evaluadas++;
                }

                Optional<OcupacionEstado> resultado =
                        controlOcupacionOrquestadorService
                                .procesarReserva(reserva, momento);


                LocalDateTime finProcesamiento = LocalDateTime.now();

                if (perteneceAR6
                        && resultado.isPresent()
                        && resultado.get().getEstadoOcupacion()
                        == EstadoOcupacion.LIBERADO
                        && liberacionesMedidas.add(
                        reserva.getIdReserva()
                )) {

                    LocalDateTime inicioControl =
                            reserva.getFechaHoraInicio();

                    if (reserva.getFechaCreacion() != null
                            && reserva.getFechaCreacion()
                            .isAfter(inicioControl)) {

                        inicioControl =
                                reserva.getFechaCreacion();
                    }

                    LocalDateTime limiteTolerancia =
                            inicioControl.plusMinutes(
                                    reserva.getToleranciaMinutos()
                            );

                    long demoraMs = Duration.between(
                            limiteTolerancia,
                            finProcesamiento
                    ).toMillis();

                    liberacionesR6Registradas++;

                    logger.info(
                            "R6_LIBERACION | reserva={} | "
                                    + "limite={} | fin_procesamiento={} | "
                                    + "demora_ms={}",
                            reserva.getCodigoReserva(),
                            limiteTolerancia,
                            finProcesamiento,
                            demoraMs
                    );
                }

            } catch (RuntimeException ex) {

                logger.error(
                        "No se pudo procesar la reserva {}",
                        reserva.getCodigoReserva(),
                        ex
                );
            }
        }

        if (medirR6 && liberacionesR6Registradas > 0) {

            long duracionCicloMs =
                    (System.nanoTime() - inicioCiclo)
                            / 1_000_000;

            logger.info(
                    "R6_CICLO_LIBERACION | run_id={} | "
                            + "reservas_evaluadas={} | "
                            + "liberaciones_registradas={} | "
                            + "duracion_ms={}",
                    numeroEjecucionR6,
                    reservasR6Evaluadas,
                    liberacionesR6Registradas,
                    duracionCicloMs
            );
        }
    }
}
