package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Pasajero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasajeroRepository extends JpaRepository<Pasajero, Integer> {

    Optional<Pasajero> findByNumeroDocumento(String numeroDocumento);
}