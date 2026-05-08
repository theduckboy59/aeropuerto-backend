package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class TripulacionDetalleResponse {

    private Integer empleadoId;

    private String codigoEmpleado;

    private String nombreCompleto;

    private Integer tipoEmpleadoId;
}