package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.AsientoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsientoVueloRepository
        extends JpaRepository<AsientoVuelo, Integer>,
        JpaSpecificationExecutor<AsientoVuelo> {

    boolean existsBySegmentoOperadoIdAndCodigoAsientoSistema(
            Integer segmentoOperadoId,
            String codigoAsientoSistema
    );

    long countBySegmentoOperadoId(
            Integer segmentoOperadoId
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE AsientoVuelo av
            SET av.segmentoOperadoId = NULL,
                av.codigoAsientoSistema = NULL,
                av.estadoAsientoId = NULL,
                av.updatedAt = CURRENT_TIMESTAMP
            WHERE av.codigoAsientoSistema = :codigoAsientoSistema
            """)
    void limpiarPorCodigoAsientoSistema(
            @Param("codigoAsientoSistema") String codigoAsientoSistema
    );
}