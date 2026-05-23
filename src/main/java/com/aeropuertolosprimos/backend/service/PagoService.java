package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.PagoRequest;
import com.aeropuertolosprimos.backend.dto.PagoResponse;

import java.util.List;

public interface PagoService {

    PagoResponse pagar(
            PagoRequest request
    );

    PagoResponse obtenerPorId(
            Integer id
    );

    List<PagoResponse> listarPorReserva(
            Integer reservaId
    );
}