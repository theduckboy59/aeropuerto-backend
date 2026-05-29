package com.aeropuertolosprimos.backend.service;

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
}