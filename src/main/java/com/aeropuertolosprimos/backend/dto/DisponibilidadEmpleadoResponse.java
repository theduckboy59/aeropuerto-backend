package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DisponibilidadEmpleadoResponse {

    private Integer id;

    private Integer empleadoId;

    private String codigoEmpleado;

    private String nombreCompleto;

    private LocalDate fecha;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private Boolean disponible;
}