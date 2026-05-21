package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class VueloOperadoResponse {

    private Integer id;

    private Integer vueloProgramadoId;

    private Integer vueloId;

    private String codigoVuelo;

    private Integer aerolineaId;

    private String aerolineaNombre;

    private Integer aeropuertoSalidaId;

    private String aeropuertoSalidaNombre;

    private String aeropuertoSalidaCodigoIata;

    private String puertaEmbarqueSalida;

    private Integer aeropuertoLlegadaId;

    private String aeropuertoLlegadaNombre;

    private String aeropuertoLlegadaCodigoIata;

    private String puertaEmbarqueLlegada;

    private LocalDate fechaSalidaProgramada;

    private LocalTime horaSalidaProgramada;

    private LocalDate fechaLlegadaProgramada;

    private LocalTime horaLlegadaProgramada;

    private Integer avionId;

    private String codigoAvion;

    private Integer tripulacionId;

    private String codigoTripulacion;

    private Integer estadoVueloId;

    private String estadoVueloNombre;

    private LocalDate fechaSalidaReal;

    private LocalTime horaSalidaReal;

    private LocalDate fechaLlegadaReal;

    private LocalTime horaLlegadaReal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}