package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.*;
import com.aeropuertolosprimos.backend.service.DisponibilidadEmpleadoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disponibilidades")
public class DisponibilidadEmpleadoController {

    private final DisponibilidadEmpleadoService service;

    public DisponibilidadEmpleadoController(
            DisponibilidadEmpleadoService service
    ) {
        this.service = service;
    }

    @PostMapping
    public DisponibilidadEmpleadoResponse crear(
            @RequestBody DisponibilidadEmpleadoRequest request
    ) {

        return service.crear(request);
    }

    @PutMapping("/{id}")
    public DisponibilidadEmpleadoResponse actualizar(
            @PathVariable Integer id,
            @RequestBody DisponibilidadEmpleadoRequest request
    ) {

        return service.actualizar(
                id,
                request
        );
    }

    @GetMapping
    public List<DisponibilidadEmpleadoResponse> listar() {

        return service.listar();
    }

    @GetMapping("/empleado/{empleadoId}")
    public DisponibilidadEmpleadoResponse obtener(
            @PathVariable Integer empleadoId
    ) {

        return service.obtenerPorEmpleado(
                empleadoId
        );
    }
}