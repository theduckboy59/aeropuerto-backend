package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.PrecioVuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrecioVueloRepository extends JpaRepository<PrecioVuelo, Integer> {

    Optional<PrecioVuelo> findFirstByVueloProgramadoIdAndClaseVueloIdAndFechaVigenciaHastaIsNullOrderByIdDesc(
            Integer vueloProgramadoId,
            Integer claseVueloId
    );

    List<PrecioVuelo> findByVueloProgramadoIdOrderByClaseVueloIdAsc(
            Integer vueloProgramadoId
    );
}