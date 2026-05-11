package com.aeropuertolosprimos.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvionResponse {

    private Integer id;

    private Integer aerolineaId;
    private String aerolineaNombre;

    private Integer estadoAvionId;
    private String estadoAvionNombre;

    private Integer modeloAvionId;
    private String modeloFabricante;
    private String modeloCodigo;
    private String modeloNombre;

    private String codigoAvion;

    private String numeroSerie;

    private Integer anio;

    private Integer filasConfiguradas;

    private Integer cantidadVuelos;

    private Integer estadoId;
}