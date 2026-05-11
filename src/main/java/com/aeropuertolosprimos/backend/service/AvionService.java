package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AvionRequest;
import com.aeropuertolosprimos.backend.dto.AvionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AvionService {

    Page<AvionResponse> findAll(
            String q,
            Integer aerolineaId,
            Integer estadoAvionId,
            Integer modeloAvionId,
            Integer estadoId,
            Integer anio,
            Pageable pageable
    );

    AvionResponse findById(Integer id);

    AvionResponse create(AvionRequest request);

    AvionResponse update(Integer id, AvionRequest request);

    void changeStatus(Integer id, Integer estadoId);

    void changeOperationalStatus(Integer id, Integer estadoAvionId);
}