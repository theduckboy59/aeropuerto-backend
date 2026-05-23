package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.CheckInRequest;
import com.aeropuertolosprimos.backend.dto.CheckInResponse;
import com.aeropuertolosprimos.backend.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService service;

    @PostMapping
    public CheckInResponse realizar(
            @RequestBody CheckInRequest request
    ) {
        return service.realizar(request);
    }

    @GetMapping("/boleto/{boletoId}")
    public CheckInResponse consultarPorBoleto(
            @PathVariable Integer boletoId
    ) {
        return service.consultarPorBoleto(boletoId);
    }

    @GetMapping("/pase/{codigoPaseAbordar}")
    public CheckInResponse consultarPorPase(
            @PathVariable String codigoPaseAbordar
    ) {
        return service.consultarPorPase(codigoPaseAbordar);
    }
}