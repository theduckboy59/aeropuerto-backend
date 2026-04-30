package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    private String username;
    private String email;
    private String password;

    private Integer tipoDocumentoId;
    private String numeroDocumento;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private Integer nacionalidadId;
    private Integer codigoAreaId;
    private String telefono;
    private String telefonoEmergencia;
    private String direccion;
}