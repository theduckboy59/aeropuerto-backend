package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class LimpiarAsientosResponse {

    private Integer avionId;

    private Long asientosEliminados;

    private String mensaje;
}