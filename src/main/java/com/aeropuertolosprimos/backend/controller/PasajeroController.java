package com.aeropuertolosprimos.backend.controller;

import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.PasajeroRequest;
import com.aeropuertolosprimos.backend.dto.PasajeroResponse;
import com.aeropuertolosprimos.backend.service.PasajeroService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/pasajeros")
@RequiredArgsConstructor
public class PasajeroController {

    private final PasajeroService service;


    @PostMapping
    public PasajeroResponse crear(@RequestBody PasajeroRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<PasajeroResponse> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String pasaporte,
            @RequestParam(required = false) Integer estadoId
    ) {
        return service.buscarConFiltros(
                nombre,
                pasaporte,
                estadoId
        );
    }

    @GetMapping("/me")
    public PasajeroResponse obtenerActual(
            Authentication authentication
    ) {

        if (authentication == null || authentication.getName() == null) {
            return service.obtenerActualPorEmail(null);
        }

        return service.obtenerActualPorEmail(authentication.getName());
    }

    @GetMapping("/{id}")
    public PasajeroResponse obtener(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public PasajeroResponse actualizar(
            @PathVariable Integer id,
            @RequestBody PasajeroRequest request
    ) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
