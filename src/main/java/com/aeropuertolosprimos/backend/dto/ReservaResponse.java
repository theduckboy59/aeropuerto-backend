package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReservaResponse {

    private Integer reservaId;

    private Integer userId;

    private Integer vueloOperadoId;

    private Integer boletoId;

    private String codigoReserva;

    private String codigoBoleto;

    private String codigoPaseAbordar;

    private String estadoReserva;

    private String estadoBoleto;

    private LocalDateTime fechaReserva;

    private Integer pagoId;

    private Integer estadoPagoId;

    private String estadoPago;

    private Integer metodoPagoId;

    private String metodoPago;

    private BigDecimal montoPago;

    private Boolean pagada;

    private Boolean pendientePago;

    private Integer facturaId;

    private Integer asientoVueloId;

    private String asiento;

    private Integer cantidadMaletas;

    private Integer cantidadPasajeros;

    private BigDecimal subtotal;

    private BigDecimal recargoTotal;

    private BigDecimal total;

    private List<ReservaBoletoItemResponse> boletos;

    private String mensaje;
}