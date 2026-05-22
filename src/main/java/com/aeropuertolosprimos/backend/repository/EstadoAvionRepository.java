package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoAvion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoAvionRepository extends JpaRepository<EstadoAvion, Integer> {

    Optional<EstadoAvion> findByNombreIgnoreCase(String nombre);
}