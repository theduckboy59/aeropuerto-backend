package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class AbordajeRequest {

    private Integer vueloOperadoId;

    private String pasaporte;

    private Integer cantidadMaletasPresentadas;

    private Integer empleadoId;

    private String tipoAbordaje;
}