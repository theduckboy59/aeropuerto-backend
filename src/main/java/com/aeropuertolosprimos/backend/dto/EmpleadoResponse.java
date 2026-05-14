package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmpleadoResponse {

    private Integer id;

    private Integer userId;

    private String username;
    private String email;

    private Integer tipoEmpleadoId;
    private Integer aerolineaId;
    private String aerolineaNombre;

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

    private Integer estadoId;
}