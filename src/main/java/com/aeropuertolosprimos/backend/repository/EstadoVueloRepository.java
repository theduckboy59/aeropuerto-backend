package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoVueloRepository extends JpaRepository<EstadoVuelo, Integer> {

    Optional<EstadoVuelo> findByNombreIgnoreCase(String nombre);
}