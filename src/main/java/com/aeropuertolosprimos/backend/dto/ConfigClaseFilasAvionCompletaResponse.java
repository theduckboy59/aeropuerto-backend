package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfigClaseFilasAvionCompletaResponse {

    private Integer avionId;

    private String codigoAvion;

    private Integer modeloAvionId;

    private String modeloFabricante;

    private String modeloCodigo;

    private String modeloNombre;

    private Integer filasConfiguradas;

    private Boolean configurado;

    private List<ConfigClaseFilasAvionResponse> configuraciones;

    private List<String> filasInhabilitadasAutomaticas;
}