package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AbordajeRequest;
import com.aeropuertolosprimos.backend.dto.AbordajeResponse;
import com.aeropuertolosprimos.backend.dto.FinalizarAbordajeResponse;

public interface AbordajeService {

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