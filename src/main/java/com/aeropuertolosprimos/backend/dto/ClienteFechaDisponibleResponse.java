package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClienteFechaDisponibleResponse {

    private LocalDate fechaSalida;

    private Long vuelosDisponibles;

    private BigDecimal precioMinimo;
}