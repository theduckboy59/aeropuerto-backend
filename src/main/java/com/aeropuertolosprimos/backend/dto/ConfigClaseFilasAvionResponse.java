package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigClaseFilasAvionResponse {

    private Integer id;

    private Integer avionId;

    private Integer claseVueloId;

    private String claseVueloNombre;

    private Integer filaDesde;

    private Integer filaHasta;

    private Boolean activo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}