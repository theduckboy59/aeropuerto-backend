package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaPasajeroItemRequest {

    private Integer pasajeroId;

    private String pasaporte;

    private String nombreCompleto;

    private LocalDate fechaNacimiento;

    private String nacionalidad;

    private String codigoArea;

    private String telefono;

    private String telefonoEmergencia;

    private String direccion;

    private Integer asientoVueloId;

    private Integer claseVueloId;

    private Integer cantidadMaletas;

    private BigDecimal precioBase;

    private Boolean requiereAsiento;

    private String tipoPasajero;

    private Integer adultoResponsablePasajeroId;
}