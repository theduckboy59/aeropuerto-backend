package com.aeropuertolosprimos.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbordajeVueloPendienteResponse {

    private Integer vueloOperadoId;

    private String codigoVuelo;

    private Integer aerolineaId;

    private Integer aeropuertoSalidaId;

    private String aeropuertoSalidaNombre;

    private String aeropuertoSalidaCodigoIata;

    private Integer aeropuertoLlegadaId;

    private String aeropuertoLlegadaNombre;

    private String aeropuertoLlegadaCodigoIata;

    private LocalDate fechaSalida;

    private LocalTime horaSalida;

    private String estadoVuelo;
}