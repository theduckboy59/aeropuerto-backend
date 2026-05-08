package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AeropuertoRequest;
import com.aeropuertolosprimos.backend.dto.AeropuertoResponse;

import java.util.List;

public interface AeropuertoService {

    AeropuertoResponse crear(AeropuertoRequest request);

    List<AeropuertoResponse> listar(
            String nombre,
            String pais,
            Integer estadoId
    );

    AeropuertoResponse obtenerPorId(Integer id);

    AeropuertoResponse actualizar(
            Integer id,
            AeropuertoRequest request
    );

    void eliminar(Integer id);
}