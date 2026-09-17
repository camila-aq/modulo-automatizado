package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Ubicacion;
import com.camila.moduloautomatizado.model.entity.Usuario;
import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.RolEnReserva;
import com.camila.moduloautomatizado.model.enums.RolUsuario;
import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;
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

import java.time.LocalDateTime;

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

        Usuario administrador =
                crearAdministradorSimulado();

        Usuario estudiante1 =
                crearEstudianteSimulado(
                        "EST0000001",
                        "10000001",
                        "Ana",
                        "Torres",
                        "ana.torres@simulado.example",
                        administrador
                );

        Usuario estudiante2 =
                crearEstudianteSimulado(
                        "EST0000002",
                        "10000002",
                        "Bruno",
                        "Salazar",
                        "bruno.salazar@simulado.example",
                        administrador
                );

        Usuario estudiante3 =
                crearEstudianteSimulado(
                        "EST0000003",
                        "10000003",
                        "Carla",
                        "Mendoza",
                        "carla.mendoza@simulado.example",
                        administrador
                );

        Usuario estudiante4 =
                crearEstudianteSimulado(
                        "EST0000004",
                        "10000004",
                        "Diego",
                        "Rojas",
                        "diego.rojas@simulado.example",
                        administrador
                );

        Ubicacion ubicacion =
                crearUbicacionSimulada(
                        administrador
                );

        Ambiente ambiente =
                crearAmbienteSimulado(
                        ubicacion,
                        administrador
                );

        crearPuntoValidacionSimulado(
                ambiente,
                administrador
        );

        Reserva reserva =
                crearReservaSimulada(
                        ambiente,
                        administrador
                );

        crearReservaUsuarioSimulado(
                reserva,
                estudiante1,
                RolEnReserva.RESPONSABLE,
                administrador
        );

        crearReservaUsuarioSimulado(
                reserva,
                estudiante2,
                RolEnReserva.INTEGRANTE,
                administrador
        );

        crearReservaUsuarioSimulado(
                reserva,
                estudiante3,
                RolEnReserva.INTEGRANTE,
                administrador
        );

        crearReservaUsuarioSimulado(
                reserva,
                estudiante4,
                RolEnReserva.INTEGRANTE,
                administrador
        );

        crearEstadoInicialReserva(
                reserva,
                administrador
        );
    }

    private Usuario crearAdministradorSimulado() {

        return usuarioRepository
                .findByCodigoUniversitario(
                        "ADM0000001"
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Usuario administrador =
                            new Usuario();

                    administrador.setCodigoUniversitario(
                            "ADM0000001"
                    );

                    administrador.setDni(
                            "00000001"
                    );

                    administrador.setNombres(
                            "Administrador"
                    );

                    administrador.setApellidos(
                            "Simulado"
                    );

                    administrador.setCorreo(
                            "administrador@simulado.example"
                    );

                    administrador.setRolUsuario(
                            RolUsuario.ADMINISTRADOR
                    );

                    administrador.setActivo(
                            true
                    );

                    administrador.setFechaCreacion(
                            momento
                    );

                    return usuarioRepository.save(
                            administrador
                    );
                });
    }

    private Usuario crearEstudianteSimulado(
            String codigoUniversitario,
            String dni,
            String nombres,
            String apellidos,
            String correo,
            Usuario administrador) {

        return usuarioRepository
                .findByCodigoUniversitario(
                        codigoUniversitario
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Usuario estudiante =
                            new Usuario();

                    estudiante.setCodigoUniversitario(
                            codigoUniversitario
                    );

                    estudiante.setDni(
                            dni
                    );

                    estudiante.setNombres(
                            nombres
                    );

                    estudiante.setApellidos(
                            apellidos
                    );

                    estudiante.setCorreo(
                            correo
                    );

                    estudiante.setRolUsuario(
                            RolUsuario.ESTUDIANTE
                    );

                    estudiante.setActivo(
                            true
                    );

                    estudiante.setFechaCreacion(
                            momento
                    );

                    estudiante.setUsuarioCreacion(
                            administrador
                    );

                    return usuarioRepository.save(
                            estudiante
                    );
                });
    }

    private Ubicacion crearUbicacionSimulada(
            Usuario administrador) {

        return ubicacionRepository
                .findByNombre(
                        "Pabellón de Estudios Simulado"
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Ubicacion ubicacion =
                            new Ubicacion();

                    ubicacion.setNombre(
                            "Pabellón de Estudios Simulado"
                    );

                    ubicacion.setActivo(
                            true
                    );

                    ubicacion.setFechaCreacion(
                            momento
                    );

                    ubicacion.setUsuarioCreacion(
                            administrador
                    );

                    return ubicacionRepository.save(
                            ubicacion
                    );
                });
    }

    private Ambiente crearAmbienteSimulado(
            Ubicacion ubicacion,
            Usuario administrador) {

        return ambienteRepository
                .findByCodigo(
                        "AMB-SIM-001"
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Ambiente ambiente =
                            new Ambiente();

                    ambiente.setUbicacion(
                            ubicacion
                    );

                    ambiente.setCodigo(
                            "AMB-SIM-001"
                    );

                    ambiente.setNombre(
                            "Sala de Estudio 101"
                    );

                    ambiente.setAbreviatura(
                            "SE101"
                    );

                    ambiente.setPiso(
                            "1"
                    );

                    ambiente.setCantidadMinima(
                            2
                    );

                    ambiente.setCantidadMaxima(
                            4
                    );

                    ambiente.setActivo(
                            true
                    );

                    ambiente.setFechaCreacion(
                            momento
                    );

                    ambiente.setUsuarioCreacion(
                            administrador
                    );

                    return ambienteRepository.save(
                            ambiente
                    );
                });
    }

    private PuntoValidacion crearPuntoValidacionSimulado(
            Ambiente ambiente,
            Usuario administrador) {

        return puntoValidacionRepository
                .findByCodigoPunto(
                        "PVAL-SIM-001"
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    PuntoValidacion puntoValidacion =
                            new PuntoValidacion();

                    puntoValidacion.setAmbiente(
                            ambiente
                    );

                    puntoValidacion.setCodigoPunto(
                            "PVAL-SIM-001"
                    );

                    puntoValidacion.setActivo(
                            true
                    );

                    puntoValidacion.setFechaCreacion(
                            momento
                    );

                    puntoValidacion.setUsuarioCreacion(
                            administrador
                    );

                    return puntoValidacionRepository.save(
                            puntoValidacion
                    );
                });
    }

    private Reserva crearReservaSimulada(
            Ambiente ambiente,
            Usuario administrador) {

        LocalDateTime momento =
                LocalDateTime.now();

        /*
         * El horario de la reserva siempre comienza
         * exactamente al inicio de una hora.
         *
         * Ejemplo:
         * si la aplicación inicia a las 14:39,
         * la reserva será de 14:00 a 16:00.
         */
        LocalDateTime inicioReserva =
                momento
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

        LocalDateTime finReserva =
                inicioReserva.plusHours(2);

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(
                                "RES-SIM-001"
                        )
                        .orElseGet(() -> {

                            Reserva nuevaReserva =
                                    new Reserva();

                            nuevaReserva.setCodigoReserva(
                                    "RES-SIM-001"
                            );

                            nuevaReserva.setAmbiente(
                                    ambiente
                            );

                            nuevaReserva.setToleranciaMinutos(
                                    ReglasControlOcupacion
                                            .MINUTOS_TOLERANCIA
                            );

                            nuevaReserva.setFechaCreacion(
                                    momento
                            );

                            nuevaReserva.setUsuarioCreacion(
                                    administrador
                            );

                            return nuevaReserva;
                        });

        /*
         * Se actualiza únicamente el horario de esta
         * reserva técnica para mantener disponible el
         * escenario de pruebas internas.
         *
         * No se modifica el horario aplicando minutos
         * de anticipación.
         */
        reserva.setFechaHoraInicio(
                inicioReserva
        );

        reserva.setFechaHoraFin(
                finReserva
        );

        return reservaRepository.save(
                reserva
        );
    }

    private ReservaUsuario crearReservaUsuarioSimulado(
            Reserva reserva,
            Usuario estudiante,
            RolEnReserva rolEnReserva,
            Usuario administrador) {

        return reservaUsuarioRepository
                .findByReservaAndUsuario(
                        reserva,
                        estudiante
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    ReservaUsuario reservaUsuario =
                            new ReservaUsuario();

                    reservaUsuario.setReserva(
                            reserva
                    );

                    reservaUsuario.setUsuario(
                            estudiante
                    );

                    reservaUsuario.setRolEnReserva(
                            rolEnReserva
                    );

                    reservaUsuario.setActivo(
                            true
                    );

                    reservaUsuario.setFechaCreacion(
                            momento
                    );

                    reservaUsuario.setUsuarioCreacion(
                            administrador
                    );

                    return reservaUsuarioRepository.save(
                            reservaUsuario
                    );
                });
    }

    private ReservaEstado crearEstadoInicialReserva(
            Reserva reserva,
            Usuario administrador) {

        return reservaEstadoRepository
                .findTopByReservaOrderByFechaHoraEstadoDesc(
                        reserva
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    ReservaEstado estado =
                            new ReservaEstado();

                    estado.setReserva(
                            reserva
                    );

                    estado.setEstadoReserva(
                            EstadoReserva.VIGENTE
                    );

                    estado.setMotivo(
                            "Estado inicial de la reserva simulada"
                    );

                    estado.setFechaHoraEstado(
                            momento
                    );

                    estado.setFechaCreacion(
                            momento
                    );

                    estado.setUsuarioCreacion(
                            administrador
                    );

                    return reservaEstadoRepository.save(
                            estado
                    );
                });
    }
}