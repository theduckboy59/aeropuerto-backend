package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoReservaRepository extends JpaRepository<EstadoReserva, Integer> {

    Optional<EstadoReserva> findByNombreIgnoreCase(String nombre);
}