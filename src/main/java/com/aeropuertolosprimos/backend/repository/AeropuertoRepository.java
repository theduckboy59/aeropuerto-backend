package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Aeropuerto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AeropuertoRepository
        extends JpaRepository<Aeropuerto, Integer> {

    List<Aeropuerto> findByEstadoId(Integer estadoId);

    List<Aeropuerto>
    findByEstadoIdAndNombreContainingIgnoreCase(
            Integer estadoId,
            String nombre
    );

    List<Aeropuerto>
    findByEstadoIdAndPaisContainingIgnoreCase(
            Integer estadoId,
            String pais
    );
}