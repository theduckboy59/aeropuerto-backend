package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.AbordajeRequest;
import com.aeropuertolosprimos.backend.dto.AbordajeResponse;
import com.aeropuertolosprimos.backend.dto.AbordajeVueloPendienteResponse;
import com.aeropuertolosprimos.backend.dto.FinalizarAbordajeResponse;
import com.aeropuertolosprimos.backend.service.AbordajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/abordaje")
@RequiredArgsConstructor
public class AbordajeController {

    private final AbordajeService service;

    @GetMapping("/vuelos-pendientes")
    public List<AbordajeVueloPendienteResponse> listarVuelosPendientes(
            @RequestParam Integer aerolineaId
    ) {
        return service.listarVuelosPendientes(aerolineaId);
    }

    @GetMapping("/buscar")
    public AbordajeResponse buscar(
            @RequestParam Integer vueloOperadoId,
            @RequestParam String pasaporte,
            @RequestParam(required = false) Integer segmentoOperadoId
    ) {
        return service.buscar(
                vueloOperadoId,
                pasaporte,
                segmentoOperadoId
        );
    }

    @PatchMapping("/registrar")
    public AbordajeResponse registrarAbordaje(
            @RequestBody AbordajeRequest request
    ) {
        return service.registrarAbordaje(request);
    }

    @PatchMapping("/vuelo/{vueloOperadoId}/finalizar")
    public FinalizarAbordajeResponse finalizarAbordaje(
            @PathVariable Integer vueloOperadoId,
            @RequestParam(required = false) Integer segmentoOperadoId
    ) {
        return service.finalizarAbordaje(
                vueloOperadoId,
                segmentoOperadoId
        );
    }
}