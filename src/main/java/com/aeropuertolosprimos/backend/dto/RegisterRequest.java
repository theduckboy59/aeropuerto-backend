package com.aeropuertolosprimos.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Size(max = 100, message = "El usuario no debe exceder 100 caracteres")
    private String username;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Email(message = "Correo electrónico inválido")
    @Size(max = 150, message = "El correo no debe exceder 150 caracteres")
    private String email;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,}$",
            message = "El formato de la contraseña debe incluir al menos una letra mayúscula, un carácter especial y un número"
    )
    private String password;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Size(max = 15, message = "El número de pasaporte no debe exceder 15 caracteres")
    private String pasaporte;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Size(max = 150, message = "El nombre completo no debe exceder 150 caracteres")
    private String nombreCompleto;

    @NotNull(message = "Debe ingresar los campos obligatorios")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Size(max = 100, message = "La nacionalidad no debe exceder 100 caracteres")
    private String nacionalidad;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Pattern(
            regexp = "^\\+?\\d{1,10}$",
            message = "El código de área telefónico es inválido"
    )
    private String codigoArea;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Pattern(
            regexp = "^\\d{8}$",
            message = "El número de teléfono debe tener 8 dígitos"
    )
    private String telefono;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Pattern(
            regexp = "^\\d{8}$",
            message = "El teléfono de emergencia debe tener 8 dígitos"
    )
    private String telefonoEmergencia;

    @NotBlank(message = "Debe ingresar los campos obligatorios")
    @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
    private String direccion;
}