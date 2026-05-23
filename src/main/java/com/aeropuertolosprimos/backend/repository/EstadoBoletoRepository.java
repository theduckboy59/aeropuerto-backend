package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoBoleto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoBoletoRepository extends JpaRepository<EstadoBoleto, Integer> {

    Optional<EstadoBoleto> findByNombreIgnoreCase(String nombre);
}