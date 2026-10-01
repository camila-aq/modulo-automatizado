package com.camila.moduloautomatizado.config;

import com.camila.moduloautomatizado.model.entity.Ambiente;
import com.camila.moduloautomatizado.model.entity.PuntoValidacion;
import com.camila.moduloautomatizado.model.entity.Ubicacion;
import com.camila.moduloautomatizado.model.entity.Usuario;

import com.camila.moduloautomatizado.model.enums.RolUsuario;

import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.PuntoValidacionRepository;
import com.camila.moduloautomatizado.repository.UbicacionRepository;
import com.camila.moduloautomatizado.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "app.test-base-data.enabled",
        havingValue = "true"
)
public class DatosBaseInitializer
        implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final AmbienteRepository ambienteRepository;
    private final PuntoValidacionRepository puntoValidacionRepository;


    private static final List<UsuarioBase> ESTUDIANTES =
            List.of(

                    new UsuarioBase(
                            "EST0000001",
                            "10000001",
                            "Ana",
                            "Torres",
                            "ana.torres@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000002",
                            "10000002",
                            "Bruno",
                            "Salazar",
                            "bruno.salazar@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000003",
                            "10000003",
                            "Carla",
                            "Mendoza",
                            "carla.mendoza@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000004",
                            "10000004",
                            "Diego",
                            "Rojas",
                            "diego.rojas@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000005",
                            "10000005",
                            "Elena",
                            "Vargas",
                            "elena.vargas@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000006",
                            "10000006",
                            "Fabio",
                            "Castro",
                            "fabio.castro@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000007",
                            "10000007",
                            "Gabriela",
                            "Medina",
                            "gabriela.medina@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000008",
                            "10000008",
                            "Hugo",
                            "Vega",
                            "hugo.vega@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000009",
                            "10000009",
                            "Irene",
                            "Soto",
                            "irene.soto@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000010",
                            "10000010",
                            "Javier",
                            "León",
                            "javier.leon@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000011",
                            "10000011",
                            "Karen",
                            "Navarro",
                            "karen.navarro@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000012",
                            "10000012",
                            "Luis",
                            "Paredes",
                            "luis.paredes@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000013",
                            "10000013",
                            "Mariana",
                            "Flores",
                            "mariana.flores@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000014",
                            "10000014",
                            "Nicolás",
                            "Herrera",
                            "nicolas.herrera@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000015",
                            "10000015",
                            "Olivia",
                            "Cárdenas",
                            "olivia.cardenas@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000016",
                            "10000016",
                            "Pedro",
                            "Ramírez",
                            "pedro.ramirez@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000017",
                            "10000017",
                            "Renata",
                            "Campos",
                            "renata.campos@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000018",
                            "10000018",
                            "Sebastián",
                            "Valdez",
                            "sebastian.valdez@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000019",
                            "10000019",
                            "Tatiana",
                            "Aguilar",
                            "tatiana.aguilar@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000020",
                            "10000020",
                            "Ulises",
                            "Peña",
                            "ulises.pena@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000021",
                            "10000021",
                            "Valeria",
                            "Espinoza",
                            "valeria.espinoza@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000022",
                            "10000022",
                            "Walter",
                            "Chávez",
                            "walter.chavez@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000023",
                            "10000023",
                            "Ximena",
                            "Fuentes",
                            "ximena.fuentes@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000024",
                            "10000024",
                            "Yahir",
                            "Molina",
                            "yahir.molina@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000025",
                            "10000025",
                            "Zoe",
                            "Reyes",
                            "zoe.reyes@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000026",
                            "10000026",
                            "Andrés",
                            "Delgado",
                            "andres.delgado@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000027",
                            "10000027",
                            "Beatriz",
                            "Miranda",
                            "beatriz.miranda@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000028",
                            "10000028",
                            "Cristian",
                            "Ortega",
                            "cristian.ortega@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000029",
                            "10000029",
                            "Daniela",
                            "Cabrera",
                            "daniela.cabrera@simulado.example"
                    ),

                    new UsuarioBase(
                            "EST0000030",
                            "10000030",
                            "Emilio",
                            "Silva",
                            "emilio.silva@simulado.example"
                    )
            );


    private static final List<AmbienteBase> AMBIENTES_CCSS =
            List.of(

                    new AmbienteBase(
                            "CCSS-AMB-001",
                            "1",
                            "S1-01",
                            "SÓTANO 1",
                            3,
                            6,
                            "PVAL-CCSS-001"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-002",
                            "2",
                            "S1-02",
                            "SÓTANO 1",
                            3,
                            6,
                            "PVAL-CCSS-002"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-003",
                            "3",
                            "S1-03",
                            "SÓTANO 1",
                            3,
                            6,
                            "PVAL-CCSS-003"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-004",
                            "4",
                            "S1-04",
                            "SÓTANO 1",
                            3,
                            6,
                            "PVAL-CCSS-004"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-005",
                            "5",
                            "S1-05",
                            "SÓTANO 1",
                            3,
                            6,
                            "PVAL-CCSS-005"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-006",
                            "6",
                            "S1-06",
                            "SÓTANO 1",
                            3,
                            6,
                            "PVAL-CCSS-006"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-007",
                            "7",
                            "S1-07",
                            "SÓTANO 1",
                            2,
                            4,
                            "PVAL-CCSS-007"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-008",
                            "8",
                            "P3-08",
                            "PISO 3",
                            3,
                            6,
                            "PVAL-CCSS-008"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-009",
                            "9",
                            "P3-09",
                            "PISO 3",
                            3,
                            6,
                            "PVAL-CCSS-009"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-010",
                            "10",
                            "P3-10",
                            "PISO 3",
                            3,
                            6,
                            "PVAL-CCSS-010"
                    ),

                    new AmbienteBase(
                            "CCSS-AMB-011",
                            "11",
                            "P3-11",
                            "PISO 3",
                            3,
                            6,
                            "PVAL-CCSS-011"
                    )
            );


    private static final List<AmbienteBase> AMBIENTES_CIA =
            List.of(

                    new AmbienteBase(
                            "CIA-AMB-001",
                            "1",
                            "P2-01",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-001"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-002",
                            "2",
                            "P2-02",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-002"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-003",
                            "3",
                            "P2-03",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-003"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-004",
                            "4",
                            "P2-04",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-004"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-005",
                            "5",
                            "P2-05",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-005"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-006",
                            "6",
                            "P2-06",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-006"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-007",
                            "7",
                            "P2-07",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-007"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-008",
                            "8",
                            "P2-08",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-008"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-009",
                            "9",
                            "P2-09",
                            "PISO 2",
                            3,
                            6,
                            "PVAL-CIA-009"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-010",
                            "10",
                            "P2-10",
                            "PISO 2",
                            3,
                            5,
                            "PVAL-CIA-010"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-011",
                            "11",
                            "P2-11",
                            "PISO 2",
                            3,
                            5,
                            "PVAL-CIA-011"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-012",
                            "12",
                            "P2-12",
                            "PISO 2",
                            3,
                            5,
                            "PVAL-CIA-012"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-013",
                            "13",
                            "P2-13",
                            "PISO 2",
                            3,
                            5,
                            "PVAL-CIA-013"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-014",
                            "14",
                            "P2-14",
                            "PISO 2",
                            3,
                            5,
                            "PVAL-CIA-014"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-015",
                            "15",
                            "P2-15",
                            "PISO 2",
                            3,
                            5,
                            "PVAL-CIA-015"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-016",
                            "16",
                            "P3-16",
                            "PISO 3",
                            3,
                            6,
                            "PVAL-CIA-016"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-019",
                            "19",
                            "P3-19",
                            "PISO 3",
                            3,
                            5,
                            "PVAL-CIA-019"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-020",
                            "20",
                            "P3-20",
                            "PISO 3",
                            3,
                            5,
                            "PVAL-CIA-020"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-021",
                            "21",
                            "P3-21",
                            "PISO 3",
                            3,
                            5,
                            "PVAL-CIA-021"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-022",
                            "22",
                            "P3-22",
                            "PISO 3",
                            3,
                            5,
                            "PVAL-CIA-022"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-023",
                            "23",
                            "P3-23",
                            "PISO 3",
                            3,
                            5,
                            "PVAL-CIA-023"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-024",
                            "24",
                            "P3-24",
                            "PISO 3",
                            3,
                            5,
                            "PVAL-CIA-024"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-025",
                            "25",
                            "P4-25",
                            "PISO 4",
                            3,
                            6,
                            "PVAL-CIA-025"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-030",
                            "30",
                            "P4-30",
                            "PISO 4",
                            4,
                            8,
                            "PVAL-CIA-030"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-031",
                            "31",
                            "P4-31",
                            "PISO 4",
                            3,
                            4,
                            "PVAL-CIA-031"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-032",
                            "32",
                            "P4-32",
                            "PISO 4",
                            3,
                            4,
                            "PVAL-CIA-032"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-033",
                            "33",
                            "P4-33",
                            "PISO 4",
                            4,
                            8,
                            "PVAL-CIA-033"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-034",
                            "34",
                            "P4-34",
                            "PISO 4",
                            3,
                            6,
                            "PVAL-CIA-034"
                    ),

                    new AmbienteBase(
                            "CIA-AMB-035",
                            "35",
                            "P4-35",
                            "PISO 4",
                            3,
                            6,
                            "PVAL-CIA-035"
                    )
            );


    public DatosBaseInitializer(
            UsuarioRepository usuarioRepository,
            UbicacionRepository ubicacionRepository,
            AmbienteRepository ambienteRepository,
            PuntoValidacionRepository puntoValidacionRepository) {

        this.usuarioRepository =
                usuarioRepository;

        this.ubicacionRepository =
                ubicacionRepository;

        this.ambienteRepository =
                ambienteRepository;

        this.puntoValidacionRepository =
                puntoValidacionRepository;
    }


    @Override
    public void run(String... args) {

        Usuario administrador =
                crearAdministrador();

        for (UsuarioBase usuarioBase : ESTUDIANTES) {

            crearEstudiante(
                    usuarioBase,
                    administrador
            );
        }


        Ubicacion cienciasSociales =
                crearUbicacion(
                        "COMPLEJO DE CIENCIAS SOCIALES"
                );

        cargarAmbientes(
                cienciasSociales,
                AMBIENTES_CCSS
        );


        Ubicacion innovacionAcademica =
                crearUbicacion(
                        "BIBLIOTECA COMPLEJO DE INNOVACIÓN ACADÉMICA"
                );

        cargarAmbientes(
                innovacionAcademica,
                AMBIENTES_CIA
        );
    }


    private Usuario crearAdministrador() {

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

                    administrador.setUsuarioCreacion(
                            null
                    );

                    return usuarioRepository.save(
                            administrador
                    );
                });
    }


    private Usuario crearEstudiante(
            UsuarioBase datos,
            Usuario administrador) {

        return usuarioRepository
                .findByCodigoUniversitario(
                        datos.codigoUniversitario()
                )
                .orElseGet(() -> {

                    LocalDateTime momento =
                            LocalDateTime.now();

                    Usuario estudiante =
                            new Usuario();

                    estudiante.setCodigoUniversitario(
                            datos.codigoUniversitario()
                    );

                    estudiante.setDni(
                            datos.dni()
                    );

                    estudiante.setNombres(
                            datos.nombres()
                    );

                    estudiante.setApellidos(
                            datos.apellidos()
                    );

                    estudiante.setCorreo(
                            datos.correo()
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


    private Ubicacion crearUbicacion(
            String nombre) {

        return ubicacionRepository
                .findByNombre(
                        nombre
                )
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

                    ubicacion.setUsuarioCreacion(
                            null
                    );

                    return ubicacionRepository.save(
                            ubicacion
                    );
                });
    }


    private void cargarAmbientes(
            Ubicacion ubicacion,
            List<AmbienteBase> ambientes) {

        for (AmbienteBase datos : ambientes) {

            Ambiente ambiente =
                    crearAmbiente(
                            ubicacion,
                            datos
                    );

            crearPuntoValidacion(
                    ambiente,
                    datos.codigoPunto()
            );
        }
    }


    private Ambiente crearAmbiente(
            Ubicacion ubicacion,
            AmbienteBase datos) {

        return ambienteRepository
                .findByCodigo(
                        datos.codigo()
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
                            datos.codigo()
                    );

                    ambiente.setNombre(
                            datos.nombre()
                    );

                    ambiente.setAbreviatura(
                            datos.abreviatura()
                    );

                    ambiente.setPiso(
                            datos.piso()
                    );

                    ambiente.setCantidadMinima(
                            datos.cantidadMinima()
                    );

                    ambiente.setCantidadMaxima(
                            datos.cantidadMaxima()
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

                    PuntoValidacion puntoValidacion =
                            new PuntoValidacion();

                    puntoValidacion.setAmbiente(
                            ambiente
                    );

                    puntoValidacion.setCodigoPunto(
                            codigoPunto
                    );

                    puntoValidacion.setActivo(
                            true
                    );

                    puntoValidacion.setFechaCreacion(
                            momento
                    );

                    puntoValidacion.setUsuarioCreacion(
                            null
                    );

                    return puntoValidacionRepository.save(
                            puntoValidacion
                    );
                });
    }


    private record UsuarioBase(
            String codigoUniversitario,
            String dni,
            String nombres,
            String apellidos,
            String correo) {
    }


    private record AmbienteBase(
            String codigo,
            String nombre,
            String abreviatura,
            String piso,
            Integer cantidadMinima,
            Integer cantidadMaxima,
            String codigoPunto) {
    }
}