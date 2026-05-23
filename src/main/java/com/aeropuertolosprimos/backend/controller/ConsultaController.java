package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ReporteService service;

    @GetMapping("/vuelo/{codigoVuelo}")
    public Map<String, Object> consultaVuelo(
            @PathVariable String codigoVuelo
    ) {
        return service.consultaVuelo(codigoVuelo);
    }
}