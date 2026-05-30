package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbordajeResponse {

    private Integer boletoId;

    private String codigoBoleto;

    private String codigoPaseAbordar;

    private Integer pasajeroId;

    private String nombrePasajero;

    private String pasaporte;

    private Integer vueloOperadoId;

    private Integer boletoSegmentoId;

    private Integer segmentoOperadoId;

    private Integer ordenSegmento;

    private Integer segmentoActualOrden;

    private Integer cantidadSegmentos;

    private String estadoBoleto;

    private String estadoBoletoSegmento;

    private String asiento;

    private Integer cantidadMaletasRegistradas;

    private Integer cantidadMaletasPresentadas;

    private BigDecimal recargoEquipaje;

    private Boolean requierePagoRecargo;

    private Integer pagoRecargoId;

    private String estadoPagoRecargo;

    private BigDecimal total;

    private String mensaje;
}