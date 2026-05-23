package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.TipoEquipaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoEquipajeRepository extends JpaRepository<TipoEquipaje, Integer> {

    Optional<TipoEquipaje> findByNombreIgnoreCase(String nombre);
}