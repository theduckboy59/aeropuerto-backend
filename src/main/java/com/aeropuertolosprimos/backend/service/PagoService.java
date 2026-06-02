package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ConfirmarPagoRequest;
import com.aeropuertolosprimos.backend.dto.PagoRequest;
import com.aeropuertolosprimos.backend.dto.PagoResponse;

import java.math.BigDecimal;
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

    PagoResponse crearPagoRecargoEquipajePendiente(
            Integer reservaId,
            BigDecimal monto
    );

    PagoResponse crearPagoReservaPendiente(
            Integer reservaId
    );

    PagoResponse confirmarPagoPendiente(
            Integer pagoId,
            ConfirmarPagoRequest request
    );
}