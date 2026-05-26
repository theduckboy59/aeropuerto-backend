package com.aeropuertolosprimos.backend.controller;
import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.dto.EmpleadoResponse;
import com.aeropuertolosprimos.backend.service.EmpleadoService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService service;

    @PostMapping("/register")
    public EmpleadoResponse crear(@RequestBody EmpleadoRequest request) {
        return service.crear(request);
    }

    @GetMapping("/empleados")
    public List<EmpleadoResponse> listar(
            @RequestParam(required = false) Integer tipoEmpleadoId,
            @RequestParam(required = false) Integer aerolineaId,
            @RequestParam(required = false) LocalDate fechaIngreso,
            @RequestParam(required = false) LocalDate fechaSalida,
            @RequestParam(required = false) Integer turnoId,
            @RequestParam(required = false) Integer rolId,
            @RequestParam(required = false) Integer nivelAccesoId,
            @RequestParam(required = false) Integer areaId
    ) {
        return service.listar(
                tipoEmpleadoId,
                aerolineaId,
                fechaIngreso,
                fechaSalida,
                turnoId,
                rolId,
                nivelAccesoId,
                areaId
        );
    }

    @GetMapping("/empleados/{id}")
    public EmpleadoResponse obtener(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/empleados/{id}")
    public EmpleadoResponse actualizar(
            @PathVariable Integer id,
            @RequestBody EmpleadoRequest request
    ) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/empleados/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}