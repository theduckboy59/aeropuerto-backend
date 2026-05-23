package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ReservaRequest;
import com.aeropuertolosprimos.backend.dto.ReservaResponse;

import java.util.List;

public interface ReservaService {

    ReservaResponse crear(
            ReservaRequest request
    );

    ReservaResponse obtenerPorId(
            Integer id
    );

    List<ReservaResponse> listarPorPasajero(
            Integer pasajeroId
    );

    ReservaResponse cancelar(
            Integer id
    );
}