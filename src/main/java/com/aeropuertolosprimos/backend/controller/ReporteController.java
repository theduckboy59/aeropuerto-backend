package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;

    @GetMapping("/consulta-vuelo/{codigoVuelo}")
    public Map<String, Object> consultaVuelo(
            @PathVariable String codigoVuelo
    ) {
        return service.consultaVuelo(codigoVuelo);
    }

    @GetMapping("/vuelos")
    public List<Map<String, Object>> vuelosPorFechaHora(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaHasta
    ) {
        return service.vuelosPorFechaHora(
                fechaDesde,
                horaDesde,
                fechaHasta,
                horaHasta
        );
    }

    @GetMapping("/pasajeros-vuelo/{codigoVuelo}")
    public List<Map<String, Object>> pasajerosPorVuelo(
            @PathVariable String codigoVuelo
    ) {
        return service.pasajerosPorVuelo(codigoVuelo);
    }

    @GetMapping("/equipaje-vuelo/{codigoVuelo}")
    public List<Map<String, Object>> equipajePorVuelo(
            @PathVariable String codigoVuelo
    ) {
        return service.equipajePorVuelo(codigoVuelo);
    }

    @GetMapping("/aviones-aerolinea/{aerolineaId}")
    public List<Map<String, Object>> avionesPorAerolinea(
            @PathVariable Integer aerolineaId
    ) {
        return service.avionesPorAerolinea(aerolineaId);
    }

    @GetMapping("/aerolineas-aeropuerto/{aeropuertoId}")
    public List<Map<String, Object>> aerolineasPorAeropuerto(
            @PathVariable Integer aeropuertoId
    ) {
        return service.aerolineasPorAeropuerto(aeropuertoId);
    }

    @GetMapping("/destinos-aerolinea/{aerolineaId}")
    public List<Map<String, Object>> destinosPorAerolinea(
            @PathVariable Integer aerolineaId
    ) {
        return service.destinosPorAerolinea(aerolineaId);
    }
}