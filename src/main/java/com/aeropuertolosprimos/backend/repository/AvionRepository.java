package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Avion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AvionRepository extends
        JpaRepository<Avion, Integer>,
        JpaSpecificationExecutor<Avion> {

    List<Avion> findByEstadoId(Integer estadoId);

    long countByAerolineaIdAndEstadoId(Integer aerolineaId, Integer estadoId);
}