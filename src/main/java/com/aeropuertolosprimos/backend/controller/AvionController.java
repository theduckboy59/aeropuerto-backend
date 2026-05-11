package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.AvionRequest;
import com.aeropuertolosprimos.backend.dto.AvionResponse;
import com.aeropuertolosprimos.backend.service.AvionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avion")
@RequiredArgsConstructor
public class AvionController {

    private final AvionService service;

    @GetMapping
    public Page<AvionResponse> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer aerolineaId,
            @RequestParam(required = false) Integer estadoAvionId,
            @RequestParam(required = false) Integer modeloAvionId,
            @RequestParam(required = false) Integer estadoId,
            @RequestParam(required = false) Integer anio,
            Pageable pageable
    ) {

        return service.findAll(
                q,
                aerolineaId,
                estadoAvionId,
                modeloAvionId,
                estadoId,
                anio,
                pageable
        );
    }

    @GetMapping("/{id}")
    public AvionResponse findById(
            @PathVariable Integer id
    ) {

        return service.findById(id);
    }

    @PostMapping
    public AvionResponse create(
            @RequestBody @Valid AvionRequest request
    ) {

        return service.create(request);
    }

    @PutMapping("/{id}")
    public AvionResponse update(
            @PathVariable Integer id,
            @RequestBody @Valid AvionRequest request
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

    @PatchMapping("/{id}/estado-operativo")
    public void changeOperationalStatus(
            @PathVariable Integer id,
            @RequestParam Integer estadoAvionId
    ) {

        service.changeOperationalStatus(id, estadoAvionId);
    }
}