package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.PasajeroRequest;
import com.aeropuertolosprimos.backend.dto.PasajeroResponse;

import java.util.List;

public interface PasajeroService {

    PasajeroResponse crear(PasajeroRequest request);

    List<PasajeroResponse> listar();

    PasajeroResponse obtenerPorId(Integer id);

    PasajeroResponse actualizar(Integer id, PasajeroRequest request);

    List<PasajeroResponse> buscar(String nombre);

    void eliminar(Integer id);
}