package com.aeropuertolosprimos.backend.controller;

import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.*;
import com.aeropuertolosprimos.backend.service.DisponibilidadEmpleadoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disponibilidades")
@RequiredArgsConstructor
public class DisponibilidadEmpleadoController {

    private final DisponibilidadEmpleadoService service;


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