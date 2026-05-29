package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ClienteVueloDisponibleResponse;
import com.aeropuertolosprimos.backend.service.ClienteVueloDisponibleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cliente/vuelos-disponibles")
@RequiredArgsConstructor
public class ClienteVueloController {

    private final ClienteVueloDisponibleService service;

    @GetMapping
    public List<ClienteVueloDisponibleResponse> listarDisponibles(
            @RequestParam Integer aeropuertoSalidaId,

            @RequestParam Integer aeropuertoLlegadaId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaSalida
    ) {

        return service.listarDisponibles(
                aeropuertoSalidaId,
                aeropuertoLlegadaId,
                fechaSalida
        );
    }

    @GetMapping("/{vueloOperadoId}")
    public ClienteVueloDisponibleResponse obtenerDetalle(
            @PathVariable Integer vueloOperadoId
    ) {

        return service.obtenerDetalle(
                vueloOperadoId
        );
    }
}