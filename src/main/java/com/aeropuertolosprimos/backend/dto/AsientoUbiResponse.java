package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsientoUbiResponse {

    private Integer id;

    private Integer avionId;

    private Integer claseVueloId;

    private String claseVueloNombre;

    private Boolean vendible;

    private Integer tipoAsientoId;

    private String tipoAsientoNombre;

    private Integer nivel;

    private Integer fila;

    private String columna;

    private String numeroAsiento;

    private Integer bloque;

    private String lado;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}