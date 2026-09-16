package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.ValidacionIngreso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidacionIngresoRepository extends JpaRepository<ValidacionIngreso, Integer> {

    boolean existsByReservaUsuario(ReservaUsuario reservaUsuario);
}