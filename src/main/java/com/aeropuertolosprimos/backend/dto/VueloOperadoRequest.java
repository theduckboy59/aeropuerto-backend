package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class VueloOperadoRequest {

    private Integer vueloProgramadoId;

    private Integer avionId;

    private Integer tripulacionId;
}