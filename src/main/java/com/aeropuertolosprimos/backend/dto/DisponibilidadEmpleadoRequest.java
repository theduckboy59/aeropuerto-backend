package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DisponibilidadEmpleadoRequest {

    private Integer empleadoId;

    private LocalDate fecha;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private Boolean disponible;
}