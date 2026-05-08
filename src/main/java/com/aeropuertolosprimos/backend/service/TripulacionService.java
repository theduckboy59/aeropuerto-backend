package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.TripulacionRequest;
import com.aeropuertolosprimos.backend.dto.TripulacionResponse;

import java.util.List;

public interface TripulacionService {

    TripulacionResponse crear(
            TripulacionRequest request
    );

    TripulacionResponse actualizarEstado(
            Integer id,
            Integer estadoTripulacionId
    );

    TripulacionResponse obtenerPorId(
            Integer id
    );

    List<TripulacionResponse> listar();

    List<TripulacionResponse> listarPorAerolinea(
            Integer aerolineaId
    );

    List<TripulacionResponse> listarDisponibles(
            Integer aerolineaId
    );
}