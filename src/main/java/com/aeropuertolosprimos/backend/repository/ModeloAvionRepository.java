package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.ModeloAvion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModeloAvionRepository extends
        JpaRepository<ModeloAvion, Integer>,
        JpaSpecificationExecutor<ModeloAvion> {

    boolean existsByFabricanteIgnoreCaseAndCodigoModeloIgnoreCase(
            String fabricante,
            String codigoModelo
    );

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM ModeloAvion m
            WHERE LOWER(m.fabricante) = LOWER(:fabricante)
            AND LOWER(m.codigoModelo) = LOWER(:codigoModelo)
            AND m.id <> :id
            """)
    boolean existsDuplicateForUpdate(
            @Param("fabricante") String fabricante,
            @Param("codigoModelo") String codigoModelo,
            @Param("id") Integer id
    );
}