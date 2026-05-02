package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmpleadoRequest {

    private String username;
    private String email;
    private String password;
    private Integer tipoEmpleadoId;
    private Integer aerolineaId;
    private String codigoEmpleado;
    private String nombreCompleto;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private Integer turnoId;
    private Integer nivelAccesoId;
    private Integer rolId;
    private Integer areaId;
    private Integer licenciaId;
    private LocalDate fechaVencimientoLicencia;
}