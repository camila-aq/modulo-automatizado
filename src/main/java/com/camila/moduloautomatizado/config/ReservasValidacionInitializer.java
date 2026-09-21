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

        this.usuarioRepository =
                usuarioRepository;

        this.ambienteRepository =
                ambienteRepository;

        this.puntoValidacionRepository =
                puntoValidacionRepository;

        this.reservaRepository =
                reservaRepository;

        this.reservaUsuarioRepository =
                reservaUsuarioRepository;

        this.reservaEstadoRepository =
                reservaEstadoRepository;
    }


    @Override
    public void run(String... args) {

        crearReservaValidacionIndividual();

        crearReservaOcupacionManual();

        crearReservaPruebaTres();


        /*
         * FUTURAS RESERVAS:
         *
         * crearReservaPruebaTres();
         * crearReservaPruebaCuatro();
         */
    }


    /* =========================================================
       RESERVA 1
       Validación individual de integrantes
       ========================================================= */

    private void crearReservaValidacionIndividual() {

        Ambiente ambiente =
                obtenerAmbiente(
                        "CCSS-AMB-003"
                );


        crearPuntoValidacion(
                ambiente,
                "PVAL-CCSS-003"
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
                        ambiente,
                        "RES-CCSS-001"
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


    /* =========================================================
       RESERVA 2
       Prueba de ocupación manual completa
       ========================================================= */

    private void crearReservaOcupacionManual() {

        Ambiente ambiente =
                obtenerAmbiente(
                        "CCSS-AMB-004"
                );


        crearPuntoValidacion(
                ambiente,
                "PVAL-CCSS-004"
        );


        Usuario daniela =
                crearEstudiante(
                        "ESTCCSS004",
                        "21000004",
                        "Daniela",
                        "Rojas",
                        "daniela.rojas@simulado.example"
                );


        Usuario luis =
                crearEstudiante(
                        "ESTCCSS005",
                        "21000005",
                        "Luis",
                        "Vega",
                        "luis.vega@simulado.example"
                );


        Usuario sofia =
                crearEstudiante(
                        "ESTCCSS006",
                        "21000006",
                        "Sofía",
                        "Castro",
                        "sofia.castro@simulado.example"
                );


        Reserva reserva =
                crearReserva(
                        ambiente,
                        "RES-CCSS-002"
                );


        crearReservaUsuario(
                reserva,
                daniela,
                RolEnReserva.RESPONSABLE
        );


        crearReservaUsuario(
                reserva,
                luis,
                RolEnReserva.INTEGRANTE
        );


        crearReservaUsuario(
                reserva,
                sofia,
                RolEnReserva.INTEGRANTE
        );


        crearEstadoInicial(
                reserva
        );
    }


    /* =========================================================
       MÉTODOS AUXILIARES REUTILIZABLES
       ========================================================= */

    private Ambiente obtenerAmbiente(
            String codigoAmbiente) {

        return ambienteRepository
                .findByCodigo(
                        codigoAmbiente
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el ambiente "
                                        + codigoAmbiente
                                        + ". Ejecute primero DatosReservasInitializer."
                        )
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
            Ambiente ambiente,
            String codigoPunto) {

        return puntoValidacionRepository
                .findByCodigoPunto(
                        codigoPunto
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
                            codigoPunto
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
            Ambiente ambiente,
            String codigoReserva) {

        LocalDateTime momento =
                LocalDateTime.now();


        /*
         * La reserva siempre inicia al comienzo
         * exacto de la hora actual.
         *
         * Ejemplo:
         *
         * aplicación iniciada a las 17:07
         *
         * inicio = 17:00
         * fin    = 19:00
         */
        LocalDateTime inicio =
                momento
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);


        LocalDateTime fin =
                inicio.plusHours(
                        2
                );


        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(
                                codigoReserva
                        )
                        .orElseGet(() -> {

                            Reserva nuevaReserva =
                                    new Reserva();

                            nuevaReserva.setCodigoReserva(
                                    codigoReserva
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


        /*
         * Se actualizan siempre las horas
         * cuando se activa el initializer.
         *
         * Así la reserva queda vigente para
         * las pruebas actuales.
         */
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

    private void crearReservaPruebaTres() {

        Ambiente ambiente =
                obtenerAmbiente(
                        "CCSS-AMB-005"
                );

        crearPuntoValidacion(
                ambiente,
                "PVAL-CCSS-005"
        );

        Usuario usuario1 =
                crearEstudiante(
                        "ESTCCSS007",
                        "21000007",
                        "Nombre1",
                        "Apellido1",
                        "usuario1@simulado.example"
                );

        Usuario usuario2 =
                crearEstudiante(
                        "ESTCCSS008",
                        "21000008",
                        "Nombre2",
                        "Apellido2",
                        "usuario2@simulado.example"
                );

        Usuario usuario3 =
                crearEstudiante(
                        "ESTCCSS009",
                        "21000009",
                        "Nombre3",
                        "Apellido3",
                        "usuario3@simulado.example"
                );

        Reserva reserva =
                crearReserva(
                        ambiente,
                        "RES-CCSS-003"
                );

        crearReservaUsuario(
                reserva,
                usuario1,
                RolEnReserva.RESPONSABLE
        );

        crearReservaUsuario(
                reserva,
                usuario2,
                RolEnReserva.INTEGRANTE
        );

        crearReservaUsuario(
                reserva,
                usuario3,
                RolEnReserva.INTEGRANTE
        );

        crearEstadoInicial(
                reserva
        );
    }
}