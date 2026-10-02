package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaEstado;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;

import com.camila.moduloautomatizado.model.enums.EstadoReserva;
import com.camila.moduloautomatizado.model.enums.RolEnReserva;

import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;

import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.ReservaEstadoRepository;
import com.camila.moduloautomatizado.repository.ReservaRepository;
import com.camila.moduloautomatizado.repository.ReservaUsuarioRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Component
@ConditionalOnProperty(
        name = "app.integration-reservation-data.enabled",
        havingValue = "true"
)
public class ReservasIntegracionTestInitializer {


    private final UsuarioRepository usuarioRepository;
    private final AmbienteRepository ambienteRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaUsuarioRepository reservaUsuarioRepository;
    private final ReservaEstadoRepository reservaEstadoRepository;


    public ReservasIntegracionTestInitializer(
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

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cargarReservasDeIntegracion() {

        // crearBloqueIntegracion01();
        // crearBloqueIntegracion02();
        // crearBloqueIntegracion03();
        // crearBloqueIntegracion04();
         crearBloqueIntegracion05();
    }


    private void crearBloqueIntegracion01() {

        crearReserva(
                "RES-INT-CCSS-001",
                "CCSS-AMB-001",
                List.of(
                        "10000001",
                        "10000002",
                        "10000003"
                )
        );

        crearReserva(
                "RES-INT-CIA-001",
                "CIA-AMB-001",
                List.of(
                        "10000004",
                        "10000005",
                        "10000006"
                )
        );
    }

    private void crearBloqueIntegracion02() {
        crearReservaFueraDeVigencia(
                "RES-INT-CCSS-EXP-001",
                "CCSS-AMB-002",
                List.of(
                        "10000007",
                        "10000008",
                        "10000009"
                )
        );
    }

    private void crearBloqueIntegracion03() {

        crearReserva(
                "RES-INT-CCSS-002",
                "CCSS-AMB-007",
                List.of(
                        "10000007",
                        "10000008"
                )
        );

        crearReserva(
                "RES-INT-CIA-002",
                "CIA-AMB-010",
                List.of(
                        "10000009",
                        "10000010",
                        "10000011",
                        "10000012"
                )
        );
    }


    private void crearBloqueIntegracion04() {

        crearReserva(
                "RES-INT-CCSS-003",
                "CCSS-AMB-003",
                List.of(
                        "10000013",
                        "10000014",
                        "10000015",
                        "10000016"
                )
        );

        crearReserva(
                "RES-INT-CIA-003",
                "CIA-AMB-030",
                List.of(
                        "10000017",
                        "10000018",
                        "10000019",
                        "10000020"
                )
        );
    }


    private void crearBloqueIntegracion05() {

        crearReserva(
                "RES-INT-CCSS-004",
                "CCSS-AMB-004",
                List.of(
                        "10000020",
                        "10000021",
                        "10000022",
                        "10000023",
                        "10000024"
                )
        );

        crearReserva(
                "RES-INT-CIA-004",
                "CIA-AMB-031",
                List.of(
                        "10000025",
                        "10000026",
                        "10000027"
                )
        );
    }


    private Reserva crearReserva(
            String codigoReserva,
            String codigoAmbiente,
            List<String> dnisIntegrantes) {

        LocalDateTime ahora = LocalDateTime.now();

        Ambiente ambiente = obtenerAmbiente(codigoAmbiente);

        validarCantidadIntegrantes(
                ambiente,
                dnisIntegrantes.size()
        );

        Usuario administrador = obtenerUsuario("00000001");

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(codigoReserva)
                        .orElseGet(() -> {
                            Reserva nuevaReserva =new Reserva();
                            nuevaReserva.setCodigoReserva(codigoReserva);
                            nuevaReserva.setFechaCreacion(ahora);
                            nuevaReserva.setUsuarioCreacion(administrador);
                            return nuevaReserva;
                        });

        reserva.setAmbiente(ambiente);

        LocalDateTime inicio = ahora.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = inicio.plusHours(1);

        reserva.setFechaHoraInicio(inicio);
        reserva.setFechaHoraFin(fin);
        reserva.setToleranciaMinutos(45);

        reserva = reservaRepository.save(reserva);

        for (int i = 0; i < dnisIntegrantes.size(); i++) {

            Usuario estudiante = obtenerUsuario(dnisIntegrantes.get(i));

            RolEnReserva rol = i == 0 ? RolEnReserva.RESPONSABLE : RolEnReserva.INTEGRANTE;

            crearAsociacionReservaUsuario(
                    reserva,
                    estudiante,
                    rol,
                    administrador,
                    ahora
            );
        }

        asegurarEstadoVigente(
                reserva,
                administrador,
                ahora
        );

        /*System.out.println(
                """

                ========================================
                RESERVA DE INTEGRACIÓN PREPARADA
                Código: %s
                Edificio: %s
                Ambiente: %s
                Inicio: %s
                Fin: %s
                Integrantes: %d
                ========================================
                """.formatted(
                        reserva.getCodigoReserva(),
                        ambiente.getUbicacion().getNombre(),
                        ambiente.getCodigo(),
                        reserva.getFechaHoraInicio(),
                        reserva.getFechaHoraFin(),
                        dnisIntegrantes.size()
                )
        );*/

        return reserva;
    }

    private ReservaUsuario crearAsociacionReservaUsuario(
            Reserva reserva,
            Usuario usuario,
            RolEnReserva rol,
            Usuario administrador,
            LocalDateTime ahora) {

        ReservaUsuario reservaUsuario =
                reservaUsuarioRepository
                        .findByReservaAndUsuario(reserva,usuario)
                        .orElseGet(ReservaUsuario::new);

        reservaUsuario.setReserva(reserva);
        reservaUsuario.setUsuario(usuario);
        reservaUsuario.setRolEnReserva(rol);
        reservaUsuario.setActivo(true);

        if (reservaUsuario.getFechaCreacion() == null) {
            reservaUsuario.setFechaCreacion(ahora);
            reservaUsuario.setUsuarioCreacion(administrador);
        }

        return reservaUsuarioRepository.save(reservaUsuario);
    }


    private void asegurarEstadoVigente(
            Reserva reserva,
            Usuario administrador,
            LocalDateTime ahora) {

        boolean yaEstaVigente =
                reservaEstadoRepository
                        .findTopByReservaOrderByFechaHoraEstadoDesc(reserva)
                        .map(estado ->
                                estado.getEstadoReserva() == EstadoReserva.VIGENTE)
                        .orElse(false);

        if (yaEstaVigente) {
            return;
        }

        ReservaEstado estado = new ReservaEstado();
        estado.setReserva(reserva);
        estado.setEstadoReserva(EstadoReserva.VIGENTE);
        estado.setMotivo("Estado vigente para pruebas de integración R3");
        estado.setFechaHoraEstado(ahora);
        estado.setFechaCreacion(ahora);
        estado.setUsuarioCreacion(administrador);

        reservaEstadoRepository.save(estado);
    }


    private Ambiente obtenerAmbiente(
            String codigoAmbiente) {

        return ambienteRepository
                .findByCodigo(codigoAmbiente)
                .orElseThrow(() ->
                        new IllegalStateException("No existe el ambiente "
                                + codigoAmbiente
                                + " en los datos base."
                        )
                );
    }


    private Usuario obtenerUsuario(
            String dni) {

        return usuarioRepository
                .findByDni(dni)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el usuario con DNI "
                                + dni
                                + " en DatosBaseInitializer."
                        )
                );
    }


    private void validarCantidadIntegrantes(
            Ambiente ambiente,
            int cantidadIntegrantes) {

        Integer minimo = ambiente.getCantidadMinima();
        Integer maximo = ambiente.getCantidadMaxima();

        if (cantidadIntegrantes < minimo || cantidadIntegrantes > maximo) {

            throw new IllegalStateException(
                    "La reserva del ambiente "
                    + ambiente.getCodigo()
                    + " tiene "
                    + cantidadIntegrantes
                    + " integrantes, pero el rango permitido es "
                    + minimo
                    + " a "
                    + maximo
                    + "."
            );
        }
    }

    private Reserva crearReservaFueraDeVigencia(
            String codigoReserva,
            String codigoAmbiente,
            List<String> dnisIntegrantes) {

        LocalDateTime ahora = LocalDateTime.now();

        Ambiente ambiente = obtenerAmbiente(codigoAmbiente);

        validarCantidadIntegrantes(ambiente,dnisIntegrantes.size());

        Usuario administrador = obtenerUsuario("00000001");

        Reserva reserva =
                reservaRepository
                        .findByCodigoReserva(codigoReserva)
                        .orElseGet(() -> {
                            Reserva nuevaReserva = new Reserva();
                            nuevaReserva.setCodigoReserva(codigoReserva);
                            nuevaReserva.setFechaCreacion(ahora);
                            nuevaReserva.setUsuarioCreacion(administrador);
                            return nuevaReserva;
                        });

        reserva.setAmbiente(ambiente);

        LocalDateTime fin = ahora.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime inicio = fin.minusHours(1);

        reserva.setFechaHoraInicio(inicio);
        reserva.setFechaHoraFin(fin);
        reserva.setToleranciaMinutos(ReglasControlOcupacion.MINUTOS_TOLERANCIA);
        reserva = reservaRepository.save(reserva);

        for (int i = 0;i < dnisIntegrantes.size();i++) {
            Usuario estudiante = obtenerUsuario(dnisIntegrantes.get(i));

            RolEnReserva rol = i == 0 ? RolEnReserva.RESPONSABLE : RolEnReserva.INTEGRANTE;

            crearAsociacionReservaUsuario(reserva,estudiante,rol,administrador,ahora);
        }

        asegurarEstadoVigente(reserva,administrador,ahora);

        return reserva;
    }
}