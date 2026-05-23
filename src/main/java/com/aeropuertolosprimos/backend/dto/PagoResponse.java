package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoResponse {

    private Integer id;

    private Integer reservaId;

    private String codigoReserva;

    private Integer metodoPagoId;

    private String metodoPago;

    private BigDecimal monto;

    private BigDecimal recargoEquipaje;

    private BigDecimal totalReserva;

    private Integer estadoPagoId;

    private String estadoPago;

    private LocalDateTime fechaPago;

    private FacturaResponse factura;

    private String mensaje;
}