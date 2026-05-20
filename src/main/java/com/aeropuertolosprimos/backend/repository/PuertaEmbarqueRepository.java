package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.PuertaEmbarque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuertaEmbarqueRepository
        extends JpaRepository<PuertaEmbarque, Integer> {

    List<PuertaEmbarque> findByAeropuertoIdAndEstadoId(
            Integer aeropuertoId,
            Integer estadoId
    );

    boolean existsByAeropuertoIdAndCodigoIgnoreCaseAndEstadoId(
            Integer aeropuertoId,
            String codigo,
            Integer estadoId
    );
}