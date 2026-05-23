package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FacturaResponse {

    private Integer id;

    private Integer pagoId;

    private String nit;

    private String nombreCliente;

    private String serie;

    private String numero;

    private String uuidFel;

    private String estadoFel;

    private LocalDateTime fechaFactura;
}