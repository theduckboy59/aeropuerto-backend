package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AerolineaRequest;
import com.aeropuertolosprimos.backend.dto.AerolineaResponse;

import java.util.List;

public interface AerolineaService {

    AerolineaResponse crear(
            AerolineaRequest request
    );

    List<AerolineaResponse> listar(
            String nombre
    );

    AerolineaResponse obtenerPorId(
            Integer id
    );

    AerolineaResponse actualizar(
            Integer id,
            AerolineaRequest request
    );

    void eliminar(
            Integer id
    );
}