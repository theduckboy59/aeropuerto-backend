package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class TripulacionResponse {

    private Integer id;

    private String codigo;

    private Integer aerolineaId;

    private String aerolineaNombre;

    private Integer estadoTripulacionId;

    private String estadoTripulacionNombre;

    private List<TripulacionDetalleResponse> empleados;
}