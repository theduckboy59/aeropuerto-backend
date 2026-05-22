package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.SegmentoOperado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SegmentoOperadoRepository extends JpaRepository<SegmentoOperado, Integer> {

    List<SegmentoOperado> findByVueloOperadoIdOrderByOrdenSegmentoAsc(
            Integer vueloOperadoId
    );

    Optional<SegmentoOperado> findByVueloOperadoIdAndOrdenSegmento(
            Integer vueloOperadoId,
            Integer ordenSegmento
    );

    boolean existsBySegmentoVueloId(
            Integer segmentoVueloId
    );

    boolean existsByVueloOperadoIdAndOrdenSegmento(
            Integer vueloOperadoId,
            Integer ordenSegmento
    );

    void deleteByVueloOperadoId(
            Integer vueloOperadoId
    );
}