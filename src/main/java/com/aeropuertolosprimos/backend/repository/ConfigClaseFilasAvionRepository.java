package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.ConfigClaseFilasAvion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfigClaseFilasAvionRepository extends JpaRepository<ConfigClaseFilasAvion, Integer> {

    @Query("""
            SELECT c
            FROM ConfigClaseFilasAvion c
            WHERE (:avionId IS NULL OR c.avionId = :avionId)
              AND (:claseVueloId IS NULL OR c.claseVueloId = :claseVueloId)
              AND (:activo IS NULL OR c.activo = :activo)
            """)
    Page<ConfigClaseFilasAvion> buscarConFiltros(
            @Param("avionId") Integer avionId,
            @Param("claseVueloId") Integer claseVueloId,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM ConfigClaseFilasAvion c
            WHERE c.avionId = :avionId
              AND c.claseVueloId = :claseVueloId
              AND c.activo = true
              AND (:idExcluir IS NULL OR c.id <> :idExcluir)
            """)
    boolean existeClaseActivaParaAvion(
            @Param("avionId") Integer avionId,
            @Param("claseVueloId") Integer claseVueloId,
            @Param("idExcluir") Integer idExcluir
    );

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM ConfigClaseFilasAvion c
            WHERE c.avionId = :avionId
              AND c.activo = true
              AND (:idExcluir IS NULL OR c.id <> :idExcluir)
              AND c.filaDesde <= :filaHasta
              AND c.filaHasta >= :filaDesde
            """)
    boolean existeCruceDeFilas(
            @Param("avionId") Integer avionId,
            @Param("filaDesde") Integer filaDesde,
            @Param("filaHasta") Integer filaHasta,
            @Param("idExcluir") Integer idExcluir
    );
}