package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class SegmentoOperadoResponse {

    private Integer id;

    private Integer vueloOperadoId;

    private Integer segmentoVueloId;

    private Integer ordenSegmento;

    private Integer aeropuertoSalidaId;

    private String aeropuertoSalidaNombre;

    private String aeropuertoSalidaCodigoIata;

    private Integer aeropuertoLlegadaId;

    private String aeropuertoLlegadaNombre;

    private String aeropuertoLlegadaCodigoIata;

    private Integer tipoSegmentoVueloId;

    private String tipoSegmentoVueloNombre;

    private LocalDate fechaSalida;

    private LocalTime horaSalida;

    private LocalDate fechaLlegada;

    private LocalTime horaLlegada;

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

    private Boolean puedeCambiarEstado;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}