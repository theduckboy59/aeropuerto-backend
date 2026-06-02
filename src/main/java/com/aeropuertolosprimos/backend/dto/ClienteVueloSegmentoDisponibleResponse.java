package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ClienteVueloSegmentoDisponibleResponse {

    private Integer segmentoOperadoId;

    private Integer segmentoVueloId;

    private Integer ordenSegmento;

    private Integer aeropuertoSalidaId;

    private String aeropuertoSalidaNombre;

    private String aeropuertoSalidaCodigoIata;

    private String aeropuertoSalidaPais;

    private String aeropuertoSalidaCiudad;

    private Integer aeropuertoLlegadaId;

    private String aeropuertoLlegadaNombre;

    private String aeropuertoLlegadaCodigoIata;

    private String aeropuertoLlegadaPais;

    private String aeropuertoLlegadaCiudad;

    private LocalDate fechaSalida;

    private LocalTime horaSalida;

    private LocalDate fechaLlegada;

    private LocalTime horaLlegada;

    private Integer avionId;

    private String codigoAvion;

    private Integer asientosDisponiblesTotal;

    private Integer asientosDisponiblesEconomica;

    private Integer asientosDisponiblesEjecutiva;
}