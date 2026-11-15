
package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.model.entity.*;
import com.camila.moduloautomatizado.model.enums.*;
import com.camila.moduloautomatizado.model.rule.ReglasControlOcupacion;
import com.camila.moduloautomatizado.repository.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(
        name = "app.r6-data.enabled",
        havingValue = "true"
)
public class DatosR6Initializer {

    private final UsuarioRepository usuarioRepository;
    private final AmbienteRepository ambienteRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaUsuarioRepository reservaUsuarioRepository;
    private final ReservaEstadoRepository reservaEstadoRepository;

    @Value("${app.r6-data.scenario:OCUPACION}")
    private String escenario;

    @Value("${app.r6-data.run-id:1}")
    private int numeroEjecucion;

    @Value("${app.r6-data.segundos-para-liberacion:60}")
    private long segundosParaLiberacion;

    public DatosR6Initializer(
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
    public void cargarDatosR6() {

        String modo = escenario.trim().toUpperCase(Locale.ROOT);

        if (!modo.equals("OCUPACION") && !modo.equals("LIBERACION")) {
            throw new IllegalArgumentException(
                    "El escenario R6 debe ser OCUPACION o LIBERACION."
            );
        }

        if (numeroEjecucion < 1 || numeroEjecucion > 999) {
            throw new IllegalArgumentException(
                    "El identificador de ejecución debe estar entre 1 y 999."
            );
        }

        if (segundosParaLiberacion < 1) {
            throw new IllegalArgumentException(
                    "El tiempo previo a la liberación debe ser positivo."
            );
        }

        List<Ambiente> ambientes = ambienteRepository.findAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .filter(a ->
                        a.getCodigo().startsWith("CCSS-AMB-")
                                || a.getCodigo().startsWith("CIA-AMB-"))
                .sorted(Comparator.comparing(Ambiente::getCodigo))
                .toList();

        if (ambientes.isEmpty()) {
            throw new IllegalStateException(
                    "No existen ambientes de prueba. Active los datos base."
            );
        }

        if (ambientes.size() > 999) {
            throw new IllegalStateException(
                    "La ejecución admite hasta 999 ambientes."
            );
        }

        int capacidadTotal = ambientes.stream()
                .mapToInt(Ambiente::getCantidadMaxima)
                .sum();

        if (capacidadTotal > 9999) {
            throw new IllegalStateException(
                    "La capacidad supera el límite de identificadores R6."
            );
        }

        String prefijo = modo.equals("OCUPACION") ? "O" : "L";

        // Evita reutilizar reservas de una ejecución anterior.
        for (int i = 0; i < ambientes.size(); i++) {

            String codigo = codigoReserva(prefijo, i + 1);

            if (reservaRepository.findByCodigoReserva(codigo).isPresent()) {
                throw new IllegalStateException(
                        "Ya existe la reserva " + codigo
                                + ". Utilice una base limpia u otro run-id."
                );
            }
        }

        Usuario administrador = usuarioRepository
                .findByCodigoUniversitario("ADM0000001")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el administrador de los datos base."
                        )
                );

        LocalDateTime ahora = LocalDateTime.now();

        int tolerancia = ReglasControlOcupacion.MINUTOS_TOLERANCIA;

        LocalDateTime inicio;

        if (modo.equals("OCUPACION")) {
            // Deja disponible casi toda la tolerancia.
            inicio = ahora.minusMinutes(1);
        } else {
            // Todas las reservas vencen al mismo tiempo.
            inicio = ahora
                    .plusSeconds(segundosParaLiberacion)
                    .minusMinutes(tolerancia);
        }

        LocalDateTime fin = inicio.plusHours(1);
        LocalDateTime creacionReserva = inicio.minusHours(1);

        if (!fin.isAfter(ahora)) {
            throw new IllegalStateException(
                    "El periodo generado no está vigente."
            );
        }

        int totalUsuarios = 0;

        for (int i = 0; i < ambientes.size(); i++) {

            Ambiente ambiente = ambientes.get(i);

            Reserva reserva = new Reserva();
            reserva.setCodigoReserva(codigoReserva(prefijo, i + 1));
            reserva.setAmbiente(ambiente);
            reserva.setFechaHoraInicio(inicio);
            reserva.setFechaHoraFin(fin);
            reserva.setToleranciaMinutos(tolerancia);
            reserva.setFechaCreacion(creacionReserva);
            reserva.setUsuarioCreacion(administrador);

            reserva = reservaRepository.save(reserva);

            ReservaEstado estado = new ReservaEstado();
            estado.setReserva(reserva);
            estado.setEstadoReserva(EstadoReserva.VIGENTE);
            estado.setMotivo("Reserva simulada para R6");
            estado.setFechaHoraEstado(creacionReserva);
            estado.setFechaCreacion(creacionReserva);
            estado.setUsuarioCreacion(administrador);

            reservaEstadoRepository.save(estado);

            for (int j = 0; j < ambiente.getCantidadMaxima(); j++) {

                totalUsuarios++;

                Usuario estudiante = crearEstudiante(
                        totalUsuarios,
                        administrador,
                        ahora
                );

                ReservaUsuario integrante = new ReservaUsuario();
                integrante.setReserva(reserva);
                integrante.setUsuario(estudiante);
                integrante.setRolEnReserva(
                        j == 0
                                ? RolEnReserva.RESPONSABLE
                                : RolEnReserva.INTEGRANTE
                );
                integrante.setActivo(true);
                integrante.setFechaCreacion(ahora);
                integrante.setUsuarioCreacion(administrador);

                reservaUsuarioRepository.save(integrante);
            }
        }

        System.out.println("======================================");
        System.out.println("DATOS R6 PREPARADOS");
        System.out.println("Escenario: " + modo);
        System.out.println("Ejecución: " + numeroEjecucion);
        System.out.println("Reservas: " + ambientes.size());
        System.out.println("Integrantes: " + totalUsuarios);
        System.out.println("Inicio: " + inicio);
        System.out.println("Límite de tolerancia: "
                + inicio.plusMinutes(tolerancia));
        System.out.println("======================================");
    }

    private String codigoReserva(String prefijo, int numeroAmbiente) {
        return String.format(
                Locale.ROOT,
                "R6%s%03d%03d",
                prefijo,
                numeroEjecucion,
                numeroAmbiente
        );
    }

    private Usuario crearEstudiante(
            int numero,
            Usuario administrador,
            LocalDateTime momento) {

        String codigoUniversitario = String.format(
                Locale.ROOT,
                "R6E%03d%04d",
                numeroEjecucion,
                numero
        );

        String dni = String.format(
                Locale.ROOT,
                "7%03d%04d",
                numeroEjecucion,
                numero
        );

        return usuarioRepository
                .findByCodigoUniversitario(codigoUniversitario)
                .orElseGet(() -> {

                    Usuario usuario = new Usuario();
                    usuario.setCodigoUniversitario(codigoUniversitario);
                    usuario.setDni(dni);
                    usuario.setNombres("Estudiante");
                    usuario.setApellidos("Simulado R6 " + numero);
                    usuario.setCorreo(
                            codigoUniversitario.toLowerCase(Locale.ROOT)
                                    + "@simulado.example"
                    );
                    usuario.setRolUsuario(RolUsuario.ESTUDIANTE);
                    usuario.setActivo(true);
                    usuario.setFechaCreacion(momento);
                    usuario.setUsuarioCreacion(administrador);

                    return usuarioRepository.save(usuario);
                });
    }
}
