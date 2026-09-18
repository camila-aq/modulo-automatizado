package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.Ubicacion;
import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.UbicacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(
        name = "app.reservation-data.enabled",
        havingValue = "true"
)
public class DatosReservasInitializer
        implements CommandLineRunner {

    private final UbicacionRepository ubicacionRepository;
    private final AmbienteRepository ambienteRepository;

    public DatosReservasInitializer(
            UbicacionRepository ubicacionRepository,
            AmbienteRepository ambienteRepository) {

        this.ubicacionRepository = ubicacionRepository;
        this.ambienteRepository = ambienteRepository;
    }

    @Override
    public void run(String... args) {

        Ubicacion complejoCienciasSociales =
                crearUbicacion(
                        "COMPLEJO DE CIENCIAS SOCIALES"
                );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-001",
                "1",
                "S1-01",
                "SÓTANO 1",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-002",
                "2",
                "S1-02",
                "SÓTANO 1",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-003",
                "3",
                "S1-03",
                "SÓTANO 1",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-004",
                "4",
                "S1-04",
                "SÓTANO 1",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-005",
                "5",
                "S1-05",
                "SÓTANO 1",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-006",
                "6",
                "S1-06",
                "SÓTANO 1",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-007",
                "7",
                "S1-07",
                "SÓTANO 1",
                2,
                4
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-008",
                "8",
                "P3-08",
                "PISO 3",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-009",
                "9",
                "P3-09",
                "PISO 3",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-010",
                "10",
                "P3-10",
                "PISO 3",
                3,
                6
        );

        crearAmbiente(
                complejoCienciasSociales,
                "CCSS-AMB-011",
                "11",
                "P3-11",
                "PISO 3",
                3,
                6
        );
    }

    private Ubicacion crearUbicacion(
            String nombre) {

        return ubicacionRepository
                .findByNombre(nombre)
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Ubicacion ubicacion =
                            new Ubicacion();

                    ubicacion.setNombre(
                            nombre
                    );

                    ubicacion.setActivo(
                            true
                    );

                    ubicacion.setFechaCreacion(
                            momento
                    );

                    /*
                     * El usuario de creación queda nulo
                     * porque estos datos corresponden
                     * al catálogo simulado inicial.
                     */
                    ubicacion.setUsuarioCreacion(
                            null
                    );

                    return ubicacionRepository.save(
                            ubicacion
                    );
                });
    }

    private Ambiente crearAmbiente(
            Ubicacion ubicacion,
            String codigo,
            String nombre,
            String abreviatura,
            String piso,
            Integer cantidadMinima,
            Integer cantidadMaxima) {

        return ambienteRepository
                .findByCodigo(codigo)
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Ambiente ambiente =
                            new Ambiente();

                    ambiente.setUbicacion(
                            ubicacion
                    );

                    ambiente.setCodigo(
                            codigo
                    );

                    ambiente.setNombre(
                            nombre
                    );

                    ambiente.setAbreviatura(
                            abreviatura
                    );

                    ambiente.setPiso(
                            piso
                    );

                    ambiente.setCantidadMinima(
                            cantidadMinima
                    );

                    ambiente.setCantidadMaxima(
                            cantidadMaxima
                    );

                    ambiente.setActivo(
                            true
                    );

                    ambiente.setFechaCreacion(
                            momento
                    );

                    ambiente.setUsuarioCreacion(
                            null
                    );

                    return ambienteRepository.save(
                            ambiente
                    );
                });
    }
}