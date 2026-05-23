package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoCheckInRepository extends JpaRepository<EstadoCheckIn, Integer> {

    Optional<EstadoCheckIn> findByNombreIgnoreCase(String nombre);
}