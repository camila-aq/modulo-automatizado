package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
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
import com.camila.moduloautomatizado.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(
        name = "app.validation-reservation-data.enabled",
        havingValue = "true"
)
public class ReservasValidacionInitializer
        implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AmbienteRepository ambienteRepository;
    private final PuntoValidacionRepository puntoValidacionRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaUsuarioRepository reservaUsuarioRepository;
    private final ReservaEstadoRepository reservaEstadoRepository;

    public ReservasValidacionInitializer(
            UsuarioRepository usuarioRepository,
            AmbienteRepository ambienteRepository,
            PuntoValidacionRepository puntoValidacionRepository,
            ReservaRepository reservaRepository,
            ReservaUsuarioRepository reservaUsuarioRepository,
            ReservaEstadoRepository reservaEstadoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.ambienteRepository = ambienteRepository;
        this.puntoValidacionRepository = puntoValidacionRepository;
        this.reservaRepository = reservaRepository;
        this.reservaUsuarioRepository = reservaUsuarioRepository;
        this.reservaEstadoRepository = reservaEstadoRepository;
    }

    @Override
    public void run(String... args) {

        /*
         * La reserva de prueba se ubicará en S1-03,
         * correspondiente al Ambiente 3 del
         * Complejo de Ciencias Sociales.
         */
        Ambiente ambiente =
                ambienteRepository
                        .findByCodigo("CCSS-AMB-003")
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe el ambiente CCSS-AMB-003. "
                                                + "Ejecute primero DatosReservasInitializer."
                                )
                        );

        crearPuntoValidacion(
                ambiente
        );

        Usuario laura =
                crearEstudiante(
                        "ESTCCSS001",
                        "21000001",
                        "Laura",
                        "Medina",
                        "laura.medina@simulado.example"
                );

        Usuario marco =
                crearEstudiante(
                        "ESTCCSS002",
                        "21000002",
                        "Marco",
                        "Ruiz",
                        "marco.ruiz@simulado.example"
                );

        Usuario paula =
                crearEstudiante(
                        "ESTCCSS003",
                        "21000003",
                        "Paula",
                        "Soto",
                        "paula.soto@simulado.example"
                );

        Reserva reserva =
                crearReserva(
                        ambiente
                );

        crearReservaUsuario(
                reserva,
                laura,
                RolEnReserva.RESPONSABLE
        );

        crearReservaUsuario(
                reserva,
                marco,
                RolEnReserva.INTEGRANTE
        );

        crearReservaUsuario(
                reserva,
                paula,
                RolEnReserva.INTEGRANTE
        );

        crearEstadoInicial(
                reserva
        );
    }

    private Usuario crearEstudiante(
            String codigoUniversitario,
            String dni,
            String nombres,
            String apellidos,
            String correo) {

        return usuarioRepository
                .findByCodigoUniversitario(
                        codigoUniversitario
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Usuario usuario =
                            new Usuario();

                    usuario.setCodigoUniversitario(
                            codigoUniversitario
                    );

                    usuario.setDni(
                            dni
                    );

                    usuario.setNombres(
                            nombres
                    );

                    usuario.setApellidos(
                            apellidos
                    );

                    usuario.setCorreo(
                            correo
                    );

                    usuario.setRolUsuario(
                            RolUsuario.ESTUDIANTE
                    );

                    usuario.setActivo(
                            true
                    );

                    usuario.setFechaCreacion(
                            momento
                    );

                    usuario.setUsuarioCreacion(
                            null
                    );

                    return usuarioRepository.save(
                            usuario
                    );
                });
    }

    private PuntoValidacion crearPuntoValidacion(
            Ambiente ambiente) {

        return puntoValidacionRepository
                .findByCodigoPunto(
                        "PVAL-CCSS-003"
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    PuntoValidacion punto =
                            new PuntoValidacion();

                    punto.setAmbiente(
                            ambiente
                    );

                    punto.setCodigoPunto(
                            "PVAL-CCSS-003"
                    );

                    punto.setActivo(
                            true
                    );

                    punto.setFechaCreacion(
                            momento
                    );

                    punto.setUsuarioCreacion(
                            null
                    );

                    return puntoValidacionRepository.save(
                            punto
                    );
                });
    }

    private Reserva crearReserva(
            Ambiente ambiente) {

        LocalDateTime momento =
                LocalDateTime.now();

        /*
         * Se mantiene vigente para las pruebas:
         * comienza al inicio de la hora actual
         * y dura dos horas.
         */
        LocalDateTime inicio =
                momento
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

        LocalDateTime fin =
                inicio.plusHours(2);

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(
                                "RES-CCSS-001"
                        )
                        .orElseGet(() -> {

                            Reserva nuevaReserva =
                                    new Reserva();

                            nuevaReserva.setCodigoReserva(
                                    "RES-CCSS-001"
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
                                    null
                            );

                            return nuevaReserva;
                        });

        reserva.setAmbiente(
                ambiente
        );

        reserva.setFechaHoraInicio(
                inicio
        );

        reserva.setFechaHoraFin(
                fin
        );

        return reservaRepository.save(
                reserva
        );
    }

    private ReservaUsuario crearReservaUsuario(
            Reserva reserva,
            Usuario usuario,
            RolEnReserva rolEnReserva) {

        return reservaUsuarioRepository
                .findByReservaAndUsuario(
                        reserva,
                        usuario
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
                            usuario
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
                            null
                    );

                    return reservaUsuarioRepository.save(
                            reservaUsuario
                    );
                });
    }

    private ReservaEstado crearEstadoInicial(
            Reserva reserva) {

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
                            "Estado inicial de la reserva de validación"
                    );

                    estado.setFechaHoraEstado(
                            momento
                    );

                    estado.setFechaCreacion(
                            momento
                    );

                    estado.setUsuarioCreacion(
                            null
                    );

                    return reservaEstadoRepository.save(
                            estado
                    );
                });
    }
}