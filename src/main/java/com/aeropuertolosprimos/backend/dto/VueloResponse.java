package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class VueloResponse {

    private Integer id;

    private Integer vueloId;

    private Integer vueloProgramadoId;

    private Integer aerolineaId;

    private String aerolineaNombre;

    private String codigoVuelo;

    private Integer estadoId;

    private String estadoNombre;

    private Integer aeropuertoSalidaId;

    private String aeropuertoSalidaNombre;

    private String aeropuertoSalidaCodigoIata;

    private String aeropuertoSalidaCodigoIcao;

    private Integer aeropuertoLlegadaId;

    private String aeropuertoLlegadaNombre;

    private String aeropuertoLlegadaCodigoIata;

    private String aeropuertoLlegadaCodigoIcao;

    private String puertaEmbarqueSalida;

    private String puertaEmbarqueLlegada;

    private LocalDate fechaSalida;

    private LocalTime horaSalida;

    private LocalDate fechaLlegada;

    private LocalTime horaLlegada;

    private BigDecimal precioEconomica;

    private BigDecimal precioEjecutiva;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}