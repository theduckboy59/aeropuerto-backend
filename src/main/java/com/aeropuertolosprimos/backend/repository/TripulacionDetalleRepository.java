package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.TripulacionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TripulacionDetalleRepository
        extends JpaRepository<TripulacionDetalle, Integer> {

    List<TripulacionDetalle> findByTripulacionId(
            Integer tripulacionId
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM TripulacionDetalle td WHERE td.tripulacionId = :tripulacionId")
    void deleteByTripulacionId(
            Integer tripulacionId
    );
}