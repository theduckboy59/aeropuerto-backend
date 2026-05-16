package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.VueloRequest;
import com.aeropuertolosprimos.backend.dto.VueloResponse;
import com.aeropuertolosprimos.backend.service.VueloService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/vuelos")
@RequiredArgsConstructor
public class VueloController {

    private final VueloService service;

    @GetMapping
    public Page<VueloResponse> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String buscarSalida,
            @RequestParam(required = false) String buscarLlegada,
            @RequestParam(required = false) Integer aerolineaId,
            @RequestParam(required = false) Integer aeropuertoSalidaId,
            @RequestParam(required = false) Integer aeropuertoLlegadaId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaSalida,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaSalida,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaLlegada,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaLlegada,

            Pageable pageable
    ) {

        return service.findAll(
                q,
                buscarSalida,
                buscarLlegada,
                aerolineaId,
                aeropuertoSalidaId,
                aeropuertoLlegadaId,
                fechaSalida,
                horaSalida,
                fechaLlegada,
                horaLlegada,
                pageable
        );
    }

    @GetMapping("/{id}")
    public VueloResponse findById(
            @PathVariable Integer id
    ) {

        return service.findById(
                id
        );
    }

    @GetMapping("/codigo/{codigoVuelo}")
    public VueloResponse findByCodigo(
            @PathVariable String codigoVuelo
    ) {

        return service.findByCodigo(
                codigoVuelo
        );
    }

    @PostMapping
    public VueloResponse create(
            @RequestBody VueloRequest request
    ) {

        return service.create(
                request
        );
    }

    @PutMapping("/{id}")
    public VueloResponse update(
            @PathVariable Integer id,
            @RequestBody VueloRequest request
    ) {

        return service.update(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Integer id
    ) {

        service.delete(
                id
        );
    }
}