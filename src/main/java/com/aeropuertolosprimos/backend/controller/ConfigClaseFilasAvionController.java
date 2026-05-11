package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionRequest;
import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionResponse;
import com.aeropuertolosprimos.backend.service.ConfigClaseFilasAvionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config-clase-filas-avion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConfigClaseFilasAvionController {

    private final ConfigClaseFilasAvionService service;

    @PostMapping
    public ConfigClaseFilasAvionResponse registrar(@RequestBody ConfigClaseFilasAvionRequest request) {
        return service.registrar(request);
    }

    @GetMapping
    public Page<ConfigClaseFilasAvionResponse> buscarConFiltros(
            @RequestParam(required = false) Integer avionId,
            @RequestParam(required = false) Integer claseVueloId,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return service.buscarConFiltros(avionId, claseVueloId, activo, pageable);
    }

    @GetMapping("/{id}")
    public ConfigClaseFilasAvionResponse buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ConfigClaseFilasAvionResponse actualizar(
            @PathVariable Integer id,
            @RequestBody ConfigClaseFilasAvionRequest request
    ) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ConfigClaseFilasAvionResponse desactivar(@PathVariable Integer id) {
        return service.desactivar(id);
    }
}