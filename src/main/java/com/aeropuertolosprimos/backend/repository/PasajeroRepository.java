package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Pasajero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasajeroRepository extends JpaRepository<Pasajero, Integer> {

    boolean existsByDpi(String dpi);
}