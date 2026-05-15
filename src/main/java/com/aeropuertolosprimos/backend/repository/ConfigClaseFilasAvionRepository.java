package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.ConfigClaseFilasAvion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfigClaseFilasAvionRepository
        extends JpaRepository<ConfigClaseFilasAvion, Integer> {

    List<ConfigClaseFilasAvion> findByAvionIdAndActivoTrueOrderByFilaDesdeAsc(
            Integer avionId
    );

    List<ConfigClaseFilasAvion> findByAvionIdAndActivoTrue(
            Integer avionId
    );
}