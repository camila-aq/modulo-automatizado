package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.Reserva;
import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaUsuarioRepository extends JpaRepository<ReservaUsuario, Integer> {

    List<ReservaUsuario> findByUsuarioAndActivoTrue(Usuario usuario);

    Optional<ReservaUsuario> findByReservaAndUsuario(Reserva reserva, Usuario usuario);
}