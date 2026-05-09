package com.aeropuertolosprimos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModeloAvionRequest {

    @NotBlank
    private String fabricante;

    @NotBlank
    private String codigoModelo;

    @NotBlank
    private String nombre;

    @NotNull
    private Integer niveles;

    @NotNull
    private Integer pasillos;

    @NotBlank
    private String configuracion;

    @NotNull
    private Integer totalColumnas;

    @NotNull
    private Integer filasMin;

    @NotNull
    private Integer filasMax;

    private Integer estadoId;
}