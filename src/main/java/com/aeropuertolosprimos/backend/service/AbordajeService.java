package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AbordajeRequest;
import com.aeropuertolosprimos.backend.dto.AbordajeResponse;
import com.aeropuertolosprimos.backend.dto.AbordajeVueloPendienteResponse;
import com.aeropuertolosprimos.backend.dto.FinalizarAbordajeResponse;

import java.util.List;

public interface AbordajeService {

    List<AbordajeVueloPendienteResponse> listarVuelosPendientes(
            Integer aerolineaId
    );

    AbordajeResponse buscar(
            Integer vueloOperadoId,
            String pasaporte
    );

    AbordajeResponse registrarAbordaje(
            AbordajeRequest request
    );

    FinalizarAbordajeResponse finalizarAbordaje(
            Integer vueloOperadoId
    );
}