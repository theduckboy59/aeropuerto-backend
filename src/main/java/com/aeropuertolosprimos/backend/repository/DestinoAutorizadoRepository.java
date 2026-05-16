package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.DestinoAutorizado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinoAutorizadoRepository
        extends JpaRepository<DestinoAutorizado, Integer> {

    List<DestinoAutorizado>
    findByEstadoId(
            Integer estadoId
    );

    List<DestinoAutorizado>
    findByAerolineaIdAndEstadoId(
            Integer aerolineaId,
            Integer estadoId
    );

    List<DestinoAutorizado>
    findByAeropuertoIdAndEstadoId(
            Integer aeropuertoId,
            Integer estadoId
    );

    boolean existsByAerolineaIdAndAeropuertoIdAndEstadoId(
            Integer aerolineaId,
            Integer aeropuertoId,
            Integer estadoId
    );
}