package com.aeropuertolosprimos.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PasajeroResponse {

    private Integer id;

    private Integer userId;
    private String username;
    private String email;

    private String pasaporte;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private String codigoArea;
    private String telefono;
    private String telefonoEmergencia;
    private String direccion;

    private String estado;
}