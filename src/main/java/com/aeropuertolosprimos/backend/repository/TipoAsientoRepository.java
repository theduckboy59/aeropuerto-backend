package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.TipoAsiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoAsientoRepository extends JpaRepository<TipoAsiento, Integer> {

    Optional<TipoAsiento> findByNombreIgnoreCase(String nombre);

    List<TipoAsiento> findAllByOrderByNombreAsc();
}