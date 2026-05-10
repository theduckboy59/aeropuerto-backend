package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ModeloAvionPreviewResponse;
import com.aeropuertolosprimos.backend.dto.ModeloAvionRequest;
import com.aeropuertolosprimos.backend.dto.ModeloAvionResponse;
import com.aeropuertolosprimos.backend.service.ModeloAvionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/modelo-avion")
@RequiredArgsConstructor
public class ModeloAvionController {

    private final ModeloAvionService service;

    @GetMapping
    public Page<ModeloAvionResponse> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer niveles,
            @RequestParam(required = false) Integer pasillos,
            @RequestParam(required = false) String configuracion,
            @RequestParam(required = false) Integer totalColumnas,
            Pageable pageable
    ) {

        return service.findAll(
                q,
                niveles,
                pasillos,
                configuracion,
                totalColumnas,
                pageable
        );
    }

    @PostMapping("/preview")
    public ModeloAvionPreviewResponse preview(
            @RequestBody @Valid ModeloAvionRequest request
    ) {

        return service.preview(request);
    }

    @GetMapping("/{id}")
    public ModeloAvionResponse findById(
            @PathVariable Integer id
    ) {

        return service.findById(id);
    }

    @PostMapping
    public ModeloAvionResponse create(
            @RequestBody @Valid ModeloAvionRequest request
    ) {

        return service.create(request);
    }

    @PutMapping("/{id}")
    public ModeloAvionResponse update(
            @PathVariable Integer id,
            @RequestBody @Valid ModeloAvionRequest request
    ) {

        return service.update(id, request);
    }

    @PatchMapping("/{id}/estado")
    public void changeStatus(
            @PathVariable Integer id,
            @RequestParam Integer estadoId
    ) {

        service.changeStatus(id, estadoId);
    }
}