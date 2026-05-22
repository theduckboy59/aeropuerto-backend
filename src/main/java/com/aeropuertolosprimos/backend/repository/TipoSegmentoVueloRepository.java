package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.TipoSegmentoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoSegmentoVueloRepository
        extends JpaRepository<TipoSegmentoVuelo, Integer> {

    Optional<TipoSegmentoVuelo> findByNombreIgnoreCase(String nombre);

    List<TipoSegmentoVuelo> findByEstadoId(Integer estadoId);
}