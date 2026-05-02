package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.model.Empleado;
import com.aeropuertolosprimos.backend.service.EmpleadoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleados")
public class EmpleadoController {

    private final EmpleadoService service;

    public EmpleadoController(EmpleadoService service) {
        this.service = service;
    }

    @PostMapping
    public Empleado crear(@RequestBody EmpleadoRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<Empleado> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Empleado obtener(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public Empleado actualizar(@PathVariable Integer id,
                               @RequestBody EmpleadoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}