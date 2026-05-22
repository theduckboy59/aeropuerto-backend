package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.AsientoVueloResponse;
import com.aeropuertolosprimos.backend.service.AsientoVueloService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asientos-vuelo")
@RequiredArgsConstructor
public class AsientoVueloController {

    private final AsientoVueloService service;

    @GetMapping
    public Page<AsientoVueloResponse> findAll(
            @RequestParam(required = false) Integer vueloOperadoId,
            @RequestParam(required = false) Integer segmentoOperadoId,
            @RequestParam(required = false) Integer estadoAsientoId,
            @RequestParam(required = false) Integer claseVueloId,
            @RequestParam(required = false) Integer tipoAsientoId,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(required = false) Integer fila,
            @RequestParam(required = false) String columna,
            @RequestParam(required = false) String numeroAsiento,
            Pageable pageable
    ) {

        return service.findAll(
                vueloOperadoId,
                segmentoOperadoId,
                estadoAsientoId,
                claseVueloId,
                tipoAsientoId,
                nivel,
                fila,
                columna,
                numeroAsiento,
                pageable
        );
    }

    @GetMapping("/{id}")
    public AsientoVueloResponse findById(
            @PathVariable Integer id
    ) {

        return service.findById(id);
    }

    @PostMapping("/generar/vuelo-operado/{vueloOperadoId}")
    public void generarAsientosParaVueloOperado(
            @PathVariable Integer vueloOperadoId
    ) {

        service.generarAsientosParaVueloOperado(
                vueloOperadoId
        );
    }

    @PostMapping("/generar/segmento-operado/{segmentoOperadoId}")
    public void generarAsientosParaSegmentoOperado(
            @PathVariable Integer segmentoOperadoId
    ) {

        service.generarAsientosParaSegmentoOperado(
                segmentoOperadoId
        );
    }

    @PatchMapping("/{id}/estado")
    public AsientoVueloResponse cambiarEstado(
            @PathVariable Integer id,
            @RequestParam Integer estadoAsientoId
    ) {

        return service.cambiarEstado(
                id,
                estadoAsientoId
        );
    }

    @PatchMapping("/{id}/estado-nombre")
    public AsientoVueloResponse cambiarEstadoPorNombre(
            @PathVariable Integer id,
            @RequestParam String estado
    ) {

        return service.cambiarEstadoPorNombre(
                id,
                estado
        );
    }
}