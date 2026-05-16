package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VueloRepository extends JpaRepository<Vuelo, Integer> {

    boolean existsByCodigoVueloIgnoreCase(
            String codigoVuelo
    );

    Optional<Vuelo> findByCodigoVueloIgnoreCase(
            String codigoVuelo
    );
}