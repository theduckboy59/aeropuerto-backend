package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClienteVueloDisponibleResponse {

    private Integer vueloOperadoId;

    private Integer vueloProgramadoId;

    private Integer vueloId;

    private String codigoVuelo;

    private Integer aerolineaId;

    private String aerolineaNombre;

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

    private String puertaEmbarqueSalida;

    private String puertaEmbarqueLlegada;

    private LocalDate fechaSalida;

    private LocalTime horaSalida;

    private LocalDate fechaLlegada;

    private LocalTime horaLlegada;

    private Long duracionMinutos;

    private BigDecimal precioEconomica;

    private BigDecimal precioEjecutiva;

    private Integer tipoSegmentoVueloId;

    private String tipoSegmentoVueloNombre;

    private Boolean requiereNuevoAsiento;

    private Integer cantidadSegmentos;

    private Boolean tuvoEscala;

    private Integer asientosDisponiblesTotal;

    private Integer asientosDisponiblesEconomica;

    private Integer asientosDisponiblesEjecutiva;

    private List<ClienteVueloSegmentoDisponibleResponse> segmentos = new ArrayList<>();
}