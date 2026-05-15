package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ConfigClaseFilasAvionRequest {

    private Integer claseVueloId;

    private Integer filaDesde;

    private Integer filaHasta;
}