package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoAbordajeVuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoAbordajeVueloRepository extends JpaRepository<EstadoAbordajeVuelo, Integer> {

    Optional<EstadoAbordajeVuelo> findByNombreIgnoreCase(String nombre);
}