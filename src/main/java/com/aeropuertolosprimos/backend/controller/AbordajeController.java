package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.AbordajeRequest;
import com.aeropuertolosprimos.backend.dto.AbordajeResponse;
import com.aeropuertolosprimos.backend.dto.FinalizarAbordajeResponse;
import com.aeropuertolosprimos.backend.service.AbordajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/abordaje")
@RequiredArgsConstructor
public class AbordajeController {

    private final AbordajeService service;

    @GetMapping("/buscar")
    public AbordajeResponse buscar(
            @RequestParam Integer vueloOperadoId,
            @RequestParam String pasaporte
    ) {
        return service.buscar(
                vueloOperadoId,
                pasaporte
        );
    }

    @PatchMapping("/registrar")
    public AbordajeResponse registrarAbordaje(
            @RequestBody AbordajeRequest request
    ) {
        return service.registrarAbordaje(
                request
        );
    }

    @PatchMapping("/vuelo/{vueloOperadoId}/finalizar")
    public FinalizarAbordajeResponse finalizarAbordaje(
            @PathVariable Integer vueloOperadoId
    ) {
        return service.finalizarAbordaje(
                vueloOperadoId
        );
    }
}