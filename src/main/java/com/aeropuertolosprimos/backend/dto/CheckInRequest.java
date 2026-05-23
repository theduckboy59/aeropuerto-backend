package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class CheckInRequest {

    private Integer boletoId;

    private String codigoPaseAbordar;

    private Integer vueloOperadoId;

    private String pasaporte;

    private String tipoCheckin;

    private Integer empleadoId;
}