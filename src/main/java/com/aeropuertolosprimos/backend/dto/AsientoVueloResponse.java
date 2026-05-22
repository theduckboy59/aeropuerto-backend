package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsientoVueloResponse {

    private Integer id;

    private Integer segmentoOperadoId;

    private Integer vueloOperadoId;

    private Integer vueloProgramadoId;

    private Integer ordenSegmento;

    private String codigoVuelo;

    private Integer avionId;

    private String codigoAvion;

    private Integer asientoUbiId;

    private Integer claseVueloId;

    private String claseVueloNombre;

    private Integer tipoAsientoId;

    private String tipoAsientoNombre;

    private Integer nivel;

    private Integer fila;

    private String columna;

    private String numeroAsiento;

    private Integer bloque;

    private String lado;

    private Integer estadoAsientoId;

    private String estadoAsientoNombre;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}