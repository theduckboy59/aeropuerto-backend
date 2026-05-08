package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoTripulacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoTripulacionRepository
        extends JpaRepository<EstadoTripulacion, Integer> {

    Optional<EstadoTripulacion> findByNombre(
            String nombre
    );
}