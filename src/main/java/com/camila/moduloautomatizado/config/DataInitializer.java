package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.UbicacionRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.simulation-data.enabled",
        havingValue = "true"
)
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final AmbienteRepository ambienteRepository;
    private final PuntoValidacionRepository puntoValidacionRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaUsuarioRepository reservaUsuarioRepository;
    private final ReservaEstadoRepository reservaEstadoRepository;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            UbicacionRepository ubicacionRepository,
            AmbienteRepository ambienteRepository,
            PuntoValidacionRepository puntoValidacionRepository,
            ReservaRepository reservaRepository,
            ReservaUsuarioRepository reservaUsuarioRepository,
            ReservaEstadoRepository reservaEstadoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.ambienteRepository = ambienteRepository;
        this.puntoValidacionRepository = puntoValidacionRepository;
        this.reservaRepository = reservaRepository;
        this.reservaUsuarioRepository = reservaUsuarioRepository;
        this.reservaEstadoRepository = reservaEstadoRepository;
    }

    @Override
    public void run(String... args) {
        // La carga de datos simulados se implementará aquí.
    }
}