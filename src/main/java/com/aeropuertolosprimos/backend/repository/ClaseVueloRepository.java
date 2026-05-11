package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.ClaseVuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaseVueloRepository extends JpaRepository<ClaseVuelo, Integer> {

    List<ClaseVuelo> findAllByOrderByNombreAsc();
}