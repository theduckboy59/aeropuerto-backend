package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReservaBoletoItemResponse {

    private Integer pasajeroId;

    private String nombrePasajero;

    private String pasaporte;

    private Integer boletoId;

    private String codigoBoleto;

    private String codigoPaseAbordar;

    private Integer asientoVueloId;

    private String asiento;

    private Integer cantidadMaletas;

    private BigDecimal total;
}