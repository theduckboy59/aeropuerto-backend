package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.ClaseVuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaseVueloRepository extends JpaRepository<ClaseVuelo, Integer> {

    List<ClaseVuelo> findAllByOrderByNombreAsc();

    Optional<ClaseVuelo> findByNombreIgnoreCase(
            String nombre
    );
}