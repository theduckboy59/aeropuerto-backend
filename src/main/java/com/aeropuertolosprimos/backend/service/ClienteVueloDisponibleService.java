package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ClienteDestinoAutorizadoResponse;
import com.aeropuertolosprimos.backend.dto.ClienteFechaDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteVueloDisponibleResponse;

import java.time.LocalDate;
import java.util.List;

public interface ClienteVueloDisponibleService {

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