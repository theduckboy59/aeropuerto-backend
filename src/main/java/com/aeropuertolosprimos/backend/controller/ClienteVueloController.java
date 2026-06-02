package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ClienteAeropuertoDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteDestinoAutorizadoResponse;
import com.aeropuertolosprimos.backend.dto.ClienteFechaDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteUbicacionDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteVueloDisponibleResponse;
import com.aeropuertolosprimos.backend.service.ClienteVueloDisponibleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cliente/vuelos-disponibles")
@RequiredArgsConstructor
public class ClienteVueloController {

    private final ClienteVueloDisponibleService service;

    @GetMapping("/origenes")
    public List<ClienteUbicacionDisponibleResponse> buscarOrigenes(
            @RequestParam(required = false) String q
    ) {

        return service.buscarOrigenes(q);
    }

    @GetMapping("/aeropuertos-salida")
    public List<ClienteAeropuertoDisponibleResponse> buscarAeropuertosSalida(
            @RequestParam(required = false) String pais,

            @RequestParam(required = false) String ciudad,

            @RequestParam(required = false) String q
    ) {

        return service.buscarAeropuertosSalida(
                pais,
                ciudad,
                q
        );
    }

    @GetMapping("/destinos-ubicaciones")
    public List<ClienteUbicacionDisponibleResponse> buscarDestinosUbicaciones(
            @RequestParam Integer aeropuertoSalidaId,

            @RequestParam(required = false) String q
    ) {

        return service.buscarDestinosUbicaciones(
                aeropuertoSalidaId,
                q
        );
    }

    @GetMapping("/aeropuertos-destino")
    public List<ClienteAeropuertoDisponibleResponse> buscarAeropuertosDestino(
            @RequestParam Integer aeropuertoSalidaId,

            @RequestParam(required = false) String pais,

            @RequestParam(required = false) String ciudad,

            @RequestParam(required = false) String q
    ) {

        return service.buscarAeropuertosDestino(
                aeropuertoSalidaId,
                pais,
                ciudad,
                q
        );
    }

    @GetMapping
    public List<ClienteVueloDisponibleResponse> listarDisponibles(
            @RequestParam Integer aeropuertoSalidaId,

            @RequestParam Integer aeropuertoLlegadaId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaSalida
    ) {

        return service.listarDisponibles(
                aeropuertoSalidaId,
                aeropuertoLlegadaId,
                fechaSalida
        );
    }

    @GetMapping("/destinos-autorizados")
    public List<ClienteDestinoAutorizadoResponse> listarDestinosAutorizados(
            @RequestParam Integer aeropuertoSalidaId
    ) {

        return service.listarDestinosAutorizados(
                aeropuertoSalidaId
        );
    }

    @GetMapping("/fechas-disponibles")
    public List<ClienteFechaDisponibleResponse> listarFechasDisponibles(
            @RequestParam Integer aeropuertoSalidaId,

            @RequestParam Integer aeropuertoLlegadaId
    ) {

        return service.listarFechasDisponibles(
                aeropuertoSalidaId,
                aeropuertoLlegadaId
        );
    }

    @GetMapping("/fechas-regreso-disponibles")
    public List<ClienteFechaDisponibleResponse> listarFechasRegresoDisponibles(
            @RequestParam Integer aeropuertoSalidaId,

            @RequestParam Integer aeropuertoLlegadaId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaSalida
    ) {

        return service.listarFechasRegresoDisponibles(
                aeropuertoSalidaId,
                aeropuertoLlegadaId,
                fechaSalida
        );
    }

    @GetMapping("/{vueloOperadoId}")
    public ClienteVueloDisponibleResponse obtenerDetalle(
            @PathVariable Integer vueloOperadoId
    ) {

        return service.obtenerDetalle(
                vueloOperadoId
        );
    }
}