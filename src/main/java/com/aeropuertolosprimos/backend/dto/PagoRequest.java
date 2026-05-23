package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoRequest {

    private Integer reservaId;

    private Integer metodoPagoId;

    private BigDecimal monto;

    private BigDecimal recargoEquipaje;

    private String nit;

    private String nombreCliente;
}