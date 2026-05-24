package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ConfirmarPagoRequest {

    private Integer metodoPagoId;

    private String nit;

    private String nombreCliente;
}