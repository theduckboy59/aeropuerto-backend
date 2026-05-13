package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PasajeroRequest {

    private String username;
    private String email;
    private String password;

    private String pasaporte;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private String codigoArea;
    private String telefono;
    private String telefonoEmergencia;
    private String direccion;
}