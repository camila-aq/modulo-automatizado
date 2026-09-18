package com.camila.moduloautomatizado.controller;

import com.camila.moduloautomatizado.dto.AmbienteReservaResponse;
import com.camila.moduloautomatizado.dto.UbicacionReservaResponse;
import com.camila.moduloautomatizado.model.entity.Ubicacion;
import com.camila.moduloautomatizado.repository.AmbienteRepository;
import com.camila.moduloautomatizado.repository.UbicacionRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservas/catalogo")
public class CatalogoReservasController {

    private final UbicacionRepository ubicacionRepository;
    private final AmbienteRepository ambienteRepository;

    public CatalogoReservasController(
            UbicacionRepository ubicacionRepository,
            AmbienteRepository ambienteRepository) {

        this.ubicacionRepository = ubicacionRepository;
        this.ambienteRepository = ambienteRepository;
    }

    @GetMapping("/ubicaciones")
    public List<UbicacionReservaResponse> listarUbicaciones() {

        return ubicacionRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(ubicacion ->
                        new UbicacionReservaResponse(
                                ubicacion.getIdUbicacion(),
                                ubicacion.getNombre()
                        )
                )
                .toList();
    }

    @GetMapping("/ubicaciones/{idUbicacion}/ambientes")
    public List<AmbienteReservaResponse> listarAmbientes(
            @PathVariable Integer idUbicacion) {

        Ubicacion ubicacion =
                ubicacionRepository
                        .findById(idUbicacion)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la ubicación."
                                )
                        );

        return ambienteRepository
                .findByUbicacionAndActivoTrueOrderByIdAmbienteAsc(
                        ubicacion
                )
                .stream()
                .map(ambiente ->
                        new AmbienteReservaResponse(
                                ambiente.getIdAmbiente(),
                                ambiente.getCodigo(),
                                ambiente.getNombre(),
                                ambiente.getAbreviatura(),
                                ambiente.getPiso(),
                                ambiente.getCantidadMinima(),
                                ambiente.getCantidadMaxima()
                        )
                )
                .toList();
    }
}