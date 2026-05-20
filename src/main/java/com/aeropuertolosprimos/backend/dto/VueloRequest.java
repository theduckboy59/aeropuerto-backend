package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VueloRequest {

    private Integer aerolineaId;

    private Integer aeropuertoSalidaId;

    private Integer aeropuertoLlegadaId;

    private String puertaEmbarqueSalida;

    private String puertaEmbarqueLlegada;

    private LocalDate fechaSalida;

    private LocalTime horaSalida;

    private LocalDate fechaLlegada;

    private LocalTime horaLlegada;
}