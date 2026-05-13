package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class GenerarAsientosResponse {

    private Integer avionId;

    private Integer modeloAvionId;

    private Integer niveles;

    private Integer filasConfiguradas;

    private Integer totalColumnas;

    private Integer totalAsientosGenerados;

    private String mensaje;
}