package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ClienteAeropuertoDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteDestinoAutorizadoResponse;
import com.aeropuertolosprimos.backend.dto.ClienteFechaDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteUbicacionDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteVueloDisponibleResponse;

import java.time.LocalDate;
import java.util.List;

public interface ClienteVueloDisponibleService {

    List<ClienteUbicacionDisponibleResponse> buscarOrigenes(
            String q
    );

    List<ClienteAeropuertoDisponibleResponse> buscarAeropuertosSalida(
            String pais,
            String ciudad,
            String q
    );

    List<ClienteUbicacionDisponibleResponse> buscarDestinosUbicaciones(
            Integer aeropuertoSalidaId,
            String q
    );

    List<ClienteAeropuertoDisponibleResponse> buscarAeropuertosDestino(
            Integer aeropuertoSalidaId,
            String pais,
            String ciudad,
            String q
    );

    List<ClienteVueloDisponibleResponse> listarDisponibles(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida
    );

    ClienteVueloDisponibleResponse obtenerDetalle(
            Integer vueloOperadoId
    );

    List<ClienteDestinoAutorizadoResponse> listarDestinosAutorizados(
            Integer aeropuertoSalidaId
    );

    List<ClienteFechaDisponibleResponse> listarFechasDisponibles(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId
    );

    List<ClienteFechaDisponibleResponse> listarFechasRegresoDisponibles(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida
    );
}