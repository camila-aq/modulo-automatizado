package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.CorreoElectronico;
import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.enums.TipoCorreo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorreoElectronicoRepository
        extends JpaRepository<CorreoElectronico, Integer> {

    boolean existsByReservaAndTipoCorreo(
            Reserva reserva,
            TipoCorreo tipoCorreo
    );

    List<CorreoElectronico> findByReservaOrderByFechaEnvioAsc(
            Reserva reserva
    );
}