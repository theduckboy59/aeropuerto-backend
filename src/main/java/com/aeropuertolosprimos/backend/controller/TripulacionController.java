package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.*;
import com.aeropuertolosprimos.backend.service.TripulacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tripulaciones")
public class TripulacionController {

    private final TripulacionService service;

    public TripulacionController(
            TripulacionService service
    ) {
        this.service = service;
    }

    @PostMapping
    public TripulacionResponse crear(
            @RequestBody TripulacionRequest request
    ) {

        return service.crear(request);
    }

    @PutMapping("/{id}/estado/{estadoId}")
    public TripulacionResponse actualizarEstado(
            @PathVariable Integer id,
            @PathVariable Integer estadoId
    ) {

        return service.actualizarEstado(
                id,
                estadoId
        );
    }

    @GetMapping
    public List<TripulacionResponse> listar() {

        return service.listar();
    }

    @GetMapping("/{id}")
    public TripulacionResponse obtenerPorId(
            @PathVariable Integer id
    ) {

        return service.obtenerPorId(
                id
        );
    }

    @GetMapping("/aerolinea/{aerolineaId}")
    public List<TripulacionResponse> listarPorAerolinea(
            @PathVariable Integer aerolineaId
    ) {

        return service.listarPorAerolinea(
                aerolineaId
        );
    }

    @GetMapping("/disponibles/{aerolineaId}")
    public List<TripulacionResponse> listarDisponibles(
            @PathVariable Integer aerolineaId
    ) {

        return service.listarDisponibles(
                aerolineaId
        );
    }
}