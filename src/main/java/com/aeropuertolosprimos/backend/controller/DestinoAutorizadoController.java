package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.DestinoAutorizadoRequest;
import com.aeropuertolosprimos.backend.dto.DestinoAutorizadoResponse;
import com.aeropuertolosprimos.backend.service.DestinoAutorizadoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/destinos-autorizados")
public class DestinoAutorizadoController {

    private final DestinoAutorizadoService service;

    public DestinoAutorizadoController(
            DestinoAutorizadoService service
    ) {
        this.service = service;
    }

    @PostMapping
    public DestinoAutorizadoResponse crear(
            @RequestBody DestinoAutorizadoRequest request
    ) {
        return service.crear(request);
    }

    @GetMapping
    public List<DestinoAutorizadoResponse> listar(

            @RequestParam(required = false)
            Integer aerolineaId,

            @RequestParam(required = false)
            Integer aeropuertoId,

            @RequestParam(required = false)
            String pais,

            @RequestParam(required = false)
            Integer estadoId
    ) {

        return service.listar(
                aerolineaId,
                aeropuertoId,
                pais,
                estadoId
        );
    }

    @GetMapping("/{id}")
    public DestinoAutorizadoResponse obtenerPorId(
            @PathVariable Integer id
    ) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public DestinoAutorizadoResponse actualizar(
            @PathVariable Integer id,
            @RequestBody DestinoAutorizadoRequest request
    ) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Integer id
    ) {
        service.eliminar(id);
    }
}