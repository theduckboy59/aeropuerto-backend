package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    Optional<MetodoPago> findByNombreIgnoreCase(String nombre);

    List<MetodoPago> findByEstadoIdOrderByNombreAsc(Integer estadoId);
}