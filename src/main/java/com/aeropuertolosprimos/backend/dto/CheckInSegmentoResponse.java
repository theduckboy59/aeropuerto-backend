package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckInSegmentoResponse {

    private Integer checkInId;

    private Integer boletoSegmentoId;

    private Integer segmentoOperadoId;

    private Integer ordenSegmento;

    private String estadoCheckin;

    private String tipoCheckin;

    private LocalDateTime fechaCheckin;

    private Integer empleadoId;
}