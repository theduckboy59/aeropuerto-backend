package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Tripulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TripulacionRepository extends
        JpaRepository<Tripulacion, Integer>,
        JpaSpecificationExecutor<Tripulacion> {

    List<Tripulacion> findByAerolineaId(Integer aerolineaId);

    List<Tripulacion> findByEstadoTripulacionId(Integer estadoTripulacionId);

    List<Tripulacion> findByAerolineaIdAndEstadoTripulacionId(
            Integer aerolineaId,
            Integer estadoTripulacionId
    );

    long count();
}