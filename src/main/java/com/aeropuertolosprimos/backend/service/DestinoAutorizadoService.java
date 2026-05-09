package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.DestinoAutorizadoRequest;
import com.aeropuertolosprimos.backend.dto.DestinoAutorizadoResponse;

import java.util.List;

public interface DestinoAutorizadoService {

    DestinoAutorizadoResponse crear(
            DestinoAutorizadoRequest request
    );

    List<DestinoAutorizadoResponse> listar(
            Integer aerolineaId,
            Integer aeropuertoId,
            String pais,
            Integer estadoId
    );

    DestinoAutorizadoResponse obtenerPorId(
            Integer id
    );

    DestinoAutorizadoResponse actualizar(
            Integer id,
            DestinoAutorizadoRequest request
    );

    void eliminar(
            Integer id
    );
}