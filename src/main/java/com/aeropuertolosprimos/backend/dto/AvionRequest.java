package com.aeropuertolosprimos.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvionRequest {

    @NotNull
    private Integer aerolineaId;

    @NotNull
    private Integer estadoAvionId;

    @NotNull
    private Integer modeloAvionId;

    private String numeroSerie;

    @NotNull
    @Min(1950)
    private Integer anio;

    @NotNull
    @Min(1)
    private Integer filasConfiguradas;

    private Integer estadoId;
}