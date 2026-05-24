package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.PuertaEmbarque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

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

    Optional<PuertaEmbarque> findFirstByAeropuertoIdAndCodigoIgnoreCaseAndEstadoId(
            Integer aeropuertoId,
            String codigo,
            Integer estadoId
    );
}