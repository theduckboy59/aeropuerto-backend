package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.EstadoEquipaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoEquipajeRepository extends JpaRepository<EstadoEquipaje, Integer> {

    Optional<EstadoEquipaje> findByNombreIgnoreCase(String nombre);
}