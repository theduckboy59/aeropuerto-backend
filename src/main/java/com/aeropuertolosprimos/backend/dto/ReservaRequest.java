package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReservaRequest {

    private Integer userId;

    private Integer pasajeroId;

    private Integer vueloOperadoId;

    private Integer segmentoOperadoId;

    private Integer asientoVueloId;

    private Integer claseVueloId;

    private Integer cantidadMaletas;

    private BigDecimal precioBase;

    private Boolean requiereAsiento;

    private List<ReservaPasajeroItemRequest> pasajeros;
}