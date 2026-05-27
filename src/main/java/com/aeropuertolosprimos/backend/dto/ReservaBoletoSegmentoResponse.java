package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ReservaBoletoSegmentoResponse {

    private Integer boletoSegmentoId;

    private Integer segmentoOperadoId;

    private Integer ordenSegmento;

    private Integer asientoVueloId;

    private String asiento;

    private Integer claseVueloId;

    private String claseVueloNombre;
}