package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;

import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.RolEnReserva;
import com.camila.moduloautomatizado.model.enums.RolUsuario;

import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;

import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
@ConditionalOnProperty(
        name = "app.r4-mode.enabled",
        havingValue = "true"
)
public class DatosR4Initializer
        implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AmbienteRepository ambienteRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaUsuarioRepository reservaUsuarioRepository;
    private final ReservaEstadoRepository reservaEstadoRepository;

    private static final List<String> CODIGOS_AMBIENTES =
            List.of(
                    "CCSS-AMB-007",
                    "CCSS-AMB-001",
                    "CCSS-AMB-002",
                    "CIA-AMB-001",
                    "CIA-AMB-002"
            );


    private static final List<UsuarioR4> USUARIOS_MEDIDOS =
            List.of(

                    new UsuarioR4(
                            "R4EST0001",
                            "31000001",
                            "Andrea",
                            "Ramos"
                    ),

                    new UsuarioR4(
                            "R4EST0002",
                            "31000002",
                            "Bruno",
                            "Vega"
                    ),

                    new UsuarioR4(
                            "R4EST0003",
                            "31000003",
                            "Carla",
                            "Soto"
                    ),

                    new UsuarioR4(
                            "R4EST0004",
                            "31000004",
                            "Diego",
                            "Núñez"
                    ),

                    new UsuarioR4(
                            "R4EST0005",
                            "31000005",
                            "Elena",
                            "Castro"
                    ),

                    new UsuarioR4(
                            "R4EST0006",
                            "31000006",
                            "Fabio",
                            "Ruiz"
                    ),

                    new UsuarioR4(
                            "R4EST0007",
                            "31000007",
                            "Gabriela",
                            "León"
                    ),

                    new UsuarioR4(
                            "R4EST0008",
                            "31000008",
                            "Hugo",
                            "Torres"
                    ),

                    new UsuarioR4(
                            "R4EST0009",
                            "31000009",
                            "Isabel",
                            "Medina"
                    ),

                    new UsuarioR4(
                            "R4EST0010",
                            "31000010",
                            "Jorge",
                            "Salazar"
                    )
            );


    public DatosR4Initializer(
            UsuarioRepository usuarioRepository,
            AmbienteRepository ambienteRepository,
            ReservaRepository reservaRepository,
            ReservaUsuarioRepository reservaUsuarioRepository,
            ReservaEstadoRepository reservaEstadoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.ambienteRepository = ambienteRepository;
        this.reservaRepository = reservaRepository;
        this.reservaUsuarioRepository = reservaUsuarioRepository;
        this.reservaEstadoRepository = reservaEstadoRepository;
    }

    @Override
    public void run(String... args) {

        Usuario administrador = crearAdministrador();

        for (int numeroReserva = 1; numeroReserva <= 5; numeroReserva++) {

            Ambiente ambiente = obtenerAmbiente(CODIGOS_AMBIENTES.get(numeroReserva - 1));

            int indiceResponsable = (numeroReserva - 1) * 2;

            int indiceIntegrante = indiceResponsable + 1;


            Usuario responsable =
                    crearEstudiante(
                            USUARIOS_MEDIDOS.get(indiceResponsable),
                            administrador
                    );


            Usuario integranteMedido =
                    crearEstudiante(
                            USUARIOS_MEDIDOS.get(indiceIntegrante),
                            administrador
                    );

            Reserva reserva =
                    crearReserva(
                            ambiente,
                            numeroReserva,
                            administrador
                    );

            crearReservaUsuario(
                    reserva,
                    responsable,
                    RolEnReserva.RESPONSABLE,
                    administrador
            );

            crearReservaUsuario(
                    reserva,
                    integranteMedido,
                    RolEnReserva.INTEGRANTE,
                    administrador
            );

            completarCantidadMinima(
                    reserva,
                    ambiente,
                    numeroReserva,
                    administrador
            );

            asegurarReservaVigente(
                    reserva,
                    administrador
            );
        }


        System.out.println(
                "Datos de R4 preparados: 5 reservas y 10 usuarios de medición."
        );
    }


    private Ambiente obtenerAmbiente(
            String codigoAmbiente) {

        return ambienteRepository
                .findByCodigo(codigoAmbiente)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el ambiente "
                                + codigoAmbiente
                                + ". Ejecute primero los datos base."
                        )
                );
    }


    private Usuario crearAdministrador() {

        return usuarioRepository
                .findByCodigoUniversitario("ADM0000001")
                .orElseGet(() -> {

                    LocalDateTime momento = LocalDateTime.now();

                    Usuario administrador = new Usuario();
                    administrador.setCodigoUniversitario("ADM0000001");
                    administrador.setDni("00000001");
                    administrador.setNombres("Administrador");
                    administrador.setApellidos("Simulado");
                    administrador.setCorreo("administrador@simulado.example");
                    administrador.setRolUsuario(RolUsuario.ADMINISTRADOR);
                    administrador.setActivo(true);
                    administrador.setFechaCreacion(momento);
                    administrador.setUsuarioCreacion(null);

                    return usuarioRepository.save(administrador);
                });
    }


    private Usuario crearEstudiante(
            UsuarioR4 datos,
            Usuario administrador) {

        return usuarioRepository
                .findByCodigoUniversitario(datos.codigoUniversitario())
                .orElseGet(() -> {

                    LocalDateTime momento = LocalDateTime.now();

                    Usuario usuario = new Usuario();
                    usuario.setCodigoUniversitario(datos.codigoUniversitario());
                    usuario.setDni(datos.dni());
                    usuario.setNombres(datos.nombres());
                    usuario.setApellidos(datos.apellidos());
                    usuario.setCorreo(datos.codigoUniversitario().toLowerCase() + "@r4.simulado.example");
                    usuario.setRolUsuario(RolUsuario.ESTUDIANTE);
                    usuario.setActivo(true);
                    usuario.setFechaCreacion(momento);
                    usuario.setUsuarioCreacion(administrador);

                    return usuarioRepository.save(usuario);
                });
    }


    private Reserva crearReserva(
            Ambiente ambiente,
            int numero,
            Usuario administrador) {

        String codigoReserva = String.format("RES-R4-%03d",numero);

        LocalDateTime momento = LocalDateTime.now();

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
                        .findByCodigoReserva(codigoReserva)
                        .orElseGet(Reserva::new);


        if (reserva.getIdReserva() == null) {
            reserva.setCodigoReserva(codigoReserva);
            reserva.setFechaCreacion(momento);
            reserva.setUsuarioCreacion(administrador);
        }

        reserva.setAmbiente(ambiente);
        reserva.setFechaHoraInicio(inicio);
        reserva.setFechaHoraFin(fin);
        reserva.setToleranciaMinutos(ReglasControlOcupacion.MINUTOS_TOLERANCIA);

        return reservaRepository.save(reserva);
    }


    private ReservaUsuario crearReservaUsuario(
            Reserva reserva,
            Usuario usuario,
            RolEnReserva rol,
            Usuario administrador) {

        ReservaUsuario reservaUsuario =
                reservaUsuarioRepository
                        .findByReservaAndUsuario(
                                reserva,
                                usuario
                        )
                        .orElseGet(ReservaUsuario::new);


        if (reservaUsuario.getIdReservaUsuario() == null) {
            reservaUsuario.setReserva(reserva);
            reservaUsuario.setUsuario(usuario);
            reservaUsuario.setFechaCreacion(LocalDateTime.now());
            reservaUsuario.setUsuarioCreacion(administrador);
        }

        reservaUsuario.setRolEnReserva(rol);
        reservaUsuario.setActivo(true);

        return reservaUsuarioRepository.save(reservaUsuario);
    }


    private void completarCantidadMinima(
            Reserva reserva,
            Ambiente ambiente,
            int numeroReserva,
            Usuario administrador) {

        int cantidadMinima =  ambiente.getCantidadMinima();


        for (int numeroIntegrante = 3; numeroIntegrante <= cantidadMinima; numeroIntegrante++) {

            String codigoUniversitario =
                    String.format(
                            "R4AUX%02d%02d",
                            numeroReserva,
                            numeroIntegrante
                    );

            String dni =
                    String.format(
                            "319%02d%03d",
                            numeroReserva,
                            numeroIntegrante
                    );

            UsuarioR4 datosAuxiliares =
                    new UsuarioR4(
                            codigoUniversitario,
                            dni,
                            "Auxiliar",
                            "R4 "
                                    + numeroReserva
                                    + "-"
                                    + numeroIntegrante
                    );

            Usuario usuarioAuxiliar =
                    crearEstudiante(
                            datosAuxiliares,
                            administrador
                    );

            crearReservaUsuario(
                    reserva,
                    usuarioAuxiliar,
                    RolEnReserva.INTEGRANTE,
                    administrador
            );
        }
    }


    private void asegurarReservaVigente(
            Reserva reserva,
            Usuario administrador) {

        boolean yaVigente =
                reservaEstadoRepository
                        .findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
                        .map(estado -> estado.getEstadoReserva() == EstadoReserva.VIGENTE)
                        .orElse(false);


        if (yaVigente) {
            return;
        }

        LocalDateTime momento = LocalDateTime.now();

        ReservaEstado estado = new ReservaEstado();
        estado.setReserva(reserva);
        estado.setEstadoReserva(EstadoReserva.VIGENTE);
        estado.setMotivo("Estado inicial para medición R4");
        estado.setFechaHoraEstado(momento);
        estado.setFechaCreacion(momento);
        estado.setUsuarioCreacion(administrador);

        reservaEstadoRepository.save(estado);
    }

    private record UsuarioR4(
            String codigoUniversitario,
            String dni,
            String nombres,
            String apellidos) {
    }
}