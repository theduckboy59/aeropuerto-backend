package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.SegmentoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SegmentoVueloRepository extends JpaRepository<SegmentoVuelo, Integer> {

    List<SegmentoVuelo> findByVueloProgramadoIdOrderByOrdenSegmentoAsc(
            Integer vueloProgramadoId
    );

    Optional<SegmentoVuelo> findByVueloProgramadoIdAndOrdenSegmento(
            Integer vueloProgramadoId,
            Integer ordenSegmento
    );

    boolean existsByVueloProgramadoId(
            Integer vueloProgramadoId
    );

    void deleteByVueloProgramadoId(
            Integer vueloProgramadoId
    );
}