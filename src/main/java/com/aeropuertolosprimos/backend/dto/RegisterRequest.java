package com.aeropuertolosprimos.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    private String username;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Email(message = "Correo electrónico inválido")
    private String email;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,}$",
            message = "El formato de la contraseña debe incluir al menos una letra mayúscula, un número y un carácter especial"
    )
    private String password;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Pattern(regexp = "^\\d+$", message = "El pasaporte solo debe contener números")
    private String pasaporte;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    private String nombreCompleto;

    @NotNull(message = "Debe ingresar los campos obligatorios")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    private String nacionalidad;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    private String codigoArea;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    private String telefono;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    private String telefonoEmergencia;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    private String direccion;
}
