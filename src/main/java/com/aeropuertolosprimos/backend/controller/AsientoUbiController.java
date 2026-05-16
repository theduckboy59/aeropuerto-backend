package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.AsientoUbiResponse;
import com.aeropuertolosprimos.backend.service.AsientoUbiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asiento-ubi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AsientoUbiController {

    private final AsientoUbiService service;

    /*
     * asiento_ubi NO tiene CRUD.
     *
     * Este controller solo permite consultar/ver asientos.
     *
     * Los asientos se generan automáticamente desde:
     * - AvionServiceImpl
     *
     * Los asientos se sincronizan automáticamente desde:
     * - ConfigClaseFilasAvionService
     * - AvionServiceImpl
     */

    /*
     * Mapa administrativo:
     * GET /asiento-ubi?avionId=1
     *
     * Solo vendibles:
     * GET /asiento-ubi?avionId=1&vendible=true
     *
     * Vendibles por clase:
     * GET /asiento-ubi?avionId=1&claseVueloId=1&vendible=true
     *
     * Solo inhabilitados:
     * GET /asiento-ubi?avionId=1&vendible=false
     */
    @GetMapping
    public Page<AsientoUbiResponse> buscarConFiltros(
            @RequestParam(required = false) Integer avionId,
            @RequestParam(required = false) Integer claseVueloId,
            @RequestParam(required = false) Integer tipoAsientoId,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(required = false) Integer fila,
            @RequestParam(required = false) String columna,
            @RequestParam(required = false) String numeroAsiento,
            @RequestParam(required = false) Boolean vendible,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("nivel"),
                        Sort.Order.asc("fila"),
                        Sort.Order.asc("columna")
                )
        );

        return service.buscarConFiltros(
                avionId,
                claseVueloId,
                tipoAsientoId,
                nivel,
                fila,
                columna,
                numeroAsiento,
                vendible,
                pageable
        );
    }

    @GetMapping("/{id}")
    public AsientoUbiResponse buscarPorId(
            @PathVariable Integer id
    ) {

        return service.buscarPorId(id);
    }
}