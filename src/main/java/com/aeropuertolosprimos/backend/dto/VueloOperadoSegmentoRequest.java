package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VueloOperadoSegmentoRequest {

    private Integer ordenSegmento;

    private Integer aeropuertoSalidaId;

    private Integer aeropuertoLlegadaId;

    private LocalDate fechaSalida;

    private LocalTime horaSalida;

    private LocalDate fechaLlegada;

    private LocalTime horaLlegada;

    private Integer avionId;

    private Integer tripulacionId;
}