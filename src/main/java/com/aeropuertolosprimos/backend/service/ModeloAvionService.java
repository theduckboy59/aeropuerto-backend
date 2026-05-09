package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ModeloAvionPreviewResponse;
import com.aeropuertolosprimos.backend.dto.ModeloAvionRequest;
import com.aeropuertolosprimos.backend.dto.ModeloAvionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ModeloAvionService {

    Page<ModeloAvionResponse> findAll(
            String q,
            Integer niveles,
            Integer pasillos,
            String configuracion,
            Integer totalColumnas,
            Pageable pageable
    );

    ModeloAvionResponse findById(Integer id);

    ModeloAvionResponse create(ModeloAvionRequest request);

    ModeloAvionResponse update(Integer id, ModeloAvionRequest request);

    void changeStatus(Integer id, Integer estadoId);

    ModeloAvionPreviewResponse preview(ModeloAvionRequest request);
}