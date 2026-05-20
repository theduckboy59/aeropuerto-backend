package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.AsientoUbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsientoUbiRepository extends JpaRepository<AsientoUbi, Integer> {

    @Query("""
            SELECT a
            FROM AsientoUbi a
            WHERE EXISTS (
                SELECT v.id
                FROM Avion v
                WHERE v.id = a.avionId
                  AND v.estadoId = 1
            )
              AND (:avionId IS NULL OR a.avionId = :avionId)
              AND (:claseVueloId IS NULL OR a.claseVueloId = :claseVueloId)
              AND (:tipoAsientoId IS NULL OR a.tipoAsientoId = :tipoAsientoId)
              AND (:nivel IS NULL OR a.nivel = :nivel)
              AND (:fila IS NULL OR a.fila = :fila)
              AND (:columna IS NULL OR a.columna = :columna)
              AND (:numeroAsiento IS NULL OR a.numeroAsiento = :numeroAsiento)
              AND (
                    :vendible IS NULL
                    OR (:vendible = true AND a.claseVueloId IS NOT NULL)
                    OR (:vendible = false AND a.claseVueloId IS NULL)
              )
            """)
    Page<AsientoUbi> buscarConFiltros(
            @Param("avionId") Integer avionId,
            @Param("claseVueloId") Integer claseVueloId,
            @Param("tipoAsientoId") Integer tipoAsientoId,
            @Param("nivel") Integer nivel,
            @Param("fila") Integer fila,
            @Param("columna") String columna,
            @Param("numeroAsiento") String numeroAsiento,
            @Param("vendible") Boolean vendible,
            Pageable pageable
    );

    long countByAvionId(Integer avionId);

    List<AsientoUbi> findByAvionId(Integer avionId);

    void deleteByAvionId(Integer avionId);
}