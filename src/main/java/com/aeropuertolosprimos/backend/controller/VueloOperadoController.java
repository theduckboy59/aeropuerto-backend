package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.VueloOperadoRequest;
import com.aeropuertolosprimos.backend.dto.VueloOperadoResponse;
import com.aeropuertolosprimos.backend.service.VueloOperadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/vuelos-operados")
@RequiredArgsConstructor
public class VueloOperadoController {

    private final VueloOperadoService service;

    @GetMapping
    public Page<VueloOperadoResponse> findAll(
            @RequestParam(required = false) Integer vueloProgramadoId,
            @RequestParam(required = false) Integer avionId,
            @RequestParam(required = false) Integer tripulacionId,
            @RequestParam(required = false) Integer estadoVueloId,
            @RequestParam(required = false) LocalDate fechaSalidaReal,
            @RequestParam(required = false) LocalDate fechaLlegadaReal,
            Pageable pageable
    ) {

        return service.findAll(
                vueloProgramadoId,
                avionId,
                tripulacionId,
                estadoVueloId,
                fechaSalidaReal,
                fechaLlegadaReal,
                pageable
        );
    }

    @GetMapping("/{id}")
    public VueloOperadoResponse findById(
            @PathVariable Integer id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public VueloOperadoResponse create(
            @RequestBody VueloOperadoRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public VueloOperadoResponse update(
            @PathVariable Integer id,
            @RequestBody VueloOperadoRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/estado")
    public VueloOperadoResponse cambiarEstado(
            @PathVariable Integer id,
            @RequestParam Integer estadoVueloId
    ) {
        return service.cambiarEstado(id, estadoVueloId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Integer id
    ) {
        service.delete(id);
    }
}