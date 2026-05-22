package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.AsientoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AsientoVueloRepository
        extends JpaRepository<AsientoVuelo, Integer>,
        JpaSpecificationExecutor<AsientoVuelo> {

    boolean existsBySegmentoOperadoIdAndAsientoUbiId(
            Integer segmentoOperadoId,
            Integer asientoUbiId
    );

    long countBySegmentoOperadoId(
            Integer segmentoOperadoId
    );
}