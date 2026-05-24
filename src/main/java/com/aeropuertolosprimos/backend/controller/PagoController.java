package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ConfirmarPagoRequest;
import com.aeropuertolosprimos.backend.dto.PagoRequest;
import com.aeropuertolosprimos.backend.dto.PagoResponse;
import com.aeropuertolosprimos.backend.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService service;

    @PostMapping
    public PagoResponse pagar(
            @RequestBody PagoRequest request
    ) {
        return service.pagar(request);
    }

    @PatchMapping("/{id}/confirmar")
    public PagoResponse confirmarPagoPendiente(
            @PathVariable Integer id,
            @RequestBody ConfirmarPagoRequest request
    ) {
        return service.confirmarPagoPendiente(
                id,
                request
        );
    }

    @GetMapping("/{id}")
    public PagoResponse obtenerPorId(
            @PathVariable Integer id
    ) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/reserva/{reservaId}")
    public List<PagoResponse> listarPorReserva(
            @PathVariable Integer reservaId
    ) {
        return service.listarPorReserva(reservaId);
    }
}