package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReservaBoletoItemResponse {

    private Integer pasajeroId;

    private String nombrePasajero;

    private String pasaporte;

    private Integer vueloOperadoId;

    private String codigoVuelo;

    private String ruta;

    private String aeropuertoSalida;

    private String aeropuertoLlegada;

    private String fechaSalida;

    private String horaSalida;

    private String fechaLlegada;

    private String horaLlegada;

    private Integer boletoId;

    private String codigoBoleto;

    private String codigoPaseAbordar;

    private Integer asientoVueloId;

    private String asiento;

    private Integer cantidadMaletas;

    private BigDecimal precioBase;

    private BigDecimal recargoEquipaje;

    private BigDecimal total;

    private String estadoBoleto;

    private List<ReservaBoletoSegmentoResponse> segmentos;
}
