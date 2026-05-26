package com.aeropuertolosprimos.backend.controller;

import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.AeropuertoRequest;
import com.aeropuertolosprimos.backend.dto.AeropuertoResponse;
import com.aeropuertolosprimos.backend.service.AeropuertoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aeropuertos")
@RequiredArgsConstructor
public class AeropuertoController {

    private final AeropuertoService service;

    @PostMapping
    public AeropuertoResponse crear(
            @RequestBody AeropuertoRequest request
    ) {
        return service.crear(request);
    }

    @GetMapping
    public List<AeropuertoResponse> listar(
            @RequestParam(required = false)
            String nombre,

            @RequestParam(required = false)
            String pais,

            @RequestParam(required = false)
            Integer estadoId
    ) {

        return service.listar(
                nombre,
                pais,
                estadoId
        );
    }

    @GetMapping("/{id}")
    public AeropuertoResponse obtenerPorId(
            @PathVariable Integer id
    ) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public AeropuertoResponse actualizar(
            @PathVariable Integer id,
            @RequestBody AeropuertoRequest request
    ) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Integer id
    ) {
        service.eliminar(id);
    }
}