package com.camila.moduloautomatizado.repository;

import com.camila.moduloautomatizado.model.entity.ReservaUsuario;
import com.camila.moduloautomatizado.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaUsuarioRepository extends JpaRepository<ReservaUsuario, Integer> {

    List<ReservaUsuario> findByUsuarioAndActivoTrue(Usuario usuario);
}