package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.model.Pasajero;
import com.aeropuertolosprimos.backend.service.PasajeroService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pasajeros")
public class PasajeroController {

    private final PasajeroService service;

    public PasajeroController(PasajeroService service) {
        this.service = service;
    }

    @PostMapping
    public Pasajero crear(@RequestBody Pasajero pasajero) {
        return service.crear(pasajero);
    }

    @GetMapping
    public List<Pasajero> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Pasajero obtener(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}