package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoAvion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoAvionRepository extends JpaRepository<EstadoAvion, Integer> {
}