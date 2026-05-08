package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Aerolinea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AerolineaRepository
        extends JpaRepository<Aerolinea, Integer> {

    List<Aerolinea> findByEstadoId(
            Integer estadoId
    );

    List<Aerolinea>
    findByEstadoIdAndNombreContainingIgnoreCase(
            Integer estadoId,
            String nombre
    );

    boolean existsByCodigoIataIgnoreCase(
            String codigoIata
    );

    boolean existsByCodigoIcaoIgnoreCase(
            String codigoIcao
    );

    Optional<Aerolinea>
    findByCodigoIataIgnoreCase(
            String codigoIata
    );

    Optional<Aerolinea>
    findByCodigoIcaoIgnoreCase(
            String codigoIcao
    );
}