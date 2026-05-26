package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Pasajero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasajeroRepository extends JpaRepository<Pasajero, Integer> {

    boolean existsByPasaporte(String pasaporte);

    Optional<Pasajero> findByPasaporte(String pasaporte);

    Optional<Pasajero> findByUser_Email(String email);

    List<Pasajero> findByEstadoId(Integer estadoId);

    List<Pasajero> findByNombreCompletoContainingIgnoreCaseAndEstadoId(
            String nombre,
            Integer estadoId
    );
}