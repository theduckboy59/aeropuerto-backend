package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);
    Optional<Rol> findByNombreIgnoreCase(String nombre);
}