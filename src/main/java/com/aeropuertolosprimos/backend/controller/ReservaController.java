package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ReservaRequest;
import com.aeropuertolosprimos.backend.dto.ReservaResponse;
import com.aeropuertolosprimos.backend.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    @PostMapping
    public ReservaResponse crear(
            @RequestBody ReservaRequest request
    ) {
        return service.crear(request);
    }

    @GetMapping("/{id}")
    public ReservaResponse obtenerPorId(
            @PathVariable Integer id
    ) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/pasajero/{pasajeroId}")
    public List<ReservaResponse> listarPorPasajero(
            @PathVariable Integer pasajeroId
    ) {
        return service.listarPorPasajero(pasajeroId);
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaResponse cancelar(
            @PathVariable Integer id
    ) {
        return service.cancelar(id);
    }
}