package com.aeropuertolosprimos.backend.controller;

import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.AerolineaRequest;
import com.aeropuertolosprimos.backend.dto.AerolineaResponse;
import com.aeropuertolosprimos.backend.service.AerolineaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aerolineas")
@RequiredArgsConstructor
public class AerolineaController {

    private final AerolineaService service;

    @PostMapping
    public AerolineaResponse crear(
            @RequestBody AerolineaRequest request
    ) {

        return service.crear(
                request
        );
    }

    @GetMapping
    public List<AerolineaResponse> listar(
            @RequestParam(required = false)
            String nombre
    ) {

        return service.listar(
                nombre
        );
    }

    @GetMapping("/{id}")
    public AerolineaResponse obtenerPorId(
            @PathVariable Integer id
    ) {

        return service.obtenerPorId(
                id
        );
    }

    @PutMapping("/{id}")
    public AerolineaResponse actualizar(
            @PathVariable Integer id,
            @RequestBody AerolineaRequest request
    ) {

        return service.actualizar(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Integer id
    ) {

        service.eliminar(
                id
        );
    }
}