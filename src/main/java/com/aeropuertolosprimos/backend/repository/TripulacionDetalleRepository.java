package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.TripulacionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripulacionDetalleRepository
        extends JpaRepository<TripulacionDetalle, Integer> {

    List<TripulacionDetalle> findByTripulacionId(
            Integer tripulacionId
    );
}