package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoPagoRepository extends JpaRepository<EstadoPago, Integer> {

    Optional<EstadoPago> findByNombreIgnoreCase(String nombre);
}