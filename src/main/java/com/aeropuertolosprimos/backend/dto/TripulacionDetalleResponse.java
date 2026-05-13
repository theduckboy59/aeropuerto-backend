package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TripulacionDetalleResponse {

    private Integer empleadoId;

    private String codigoEmpleado;

    private String nombreCompleto;

    private Integer tipoEmpleadoId;

    private String tipoEmpleadoNombre;

    private Integer licenciaId;

    private String licenciaNombre;

    private LocalDate fechaVencimientoLicencia;
}