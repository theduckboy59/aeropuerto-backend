package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoAsiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoAsientoRepository
        extends JpaRepository<EstadoAsiento, Integer> {

    Optional<EstadoAsiento> findByNombreIgnoreCase(String nombre);
}