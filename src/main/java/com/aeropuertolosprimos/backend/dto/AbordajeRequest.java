package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AbordajeRequest {

    private Integer vueloOperadoId;

    private Integer segmentoOperadoId;

    private String pasaporte;

    private Integer cantidadMaletasPresentadas;

    private List<AbordajeEquipajeRequest> equipajes;

    private Integer empleadoId;

    private String tipoAbordaje;
}