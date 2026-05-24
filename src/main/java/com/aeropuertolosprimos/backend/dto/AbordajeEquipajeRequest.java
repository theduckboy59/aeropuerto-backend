package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbordajeEquipajeRequest {

    private Integer numeroMaleta;

    private BigDecimal peso;
}