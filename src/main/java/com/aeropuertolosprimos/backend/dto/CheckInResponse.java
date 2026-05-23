package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckInResponse {

    private Integer boletoId;

    private String codigoBoleto;

    private String codigoPaseAbordar;

    private Integer reservaId;

    private String codigoReserva;

    private Integer pasajeroId;

    private String nombrePasajero;

    private String pasaporte;

    private Integer vueloOperadoId;

    private String asiento;

    private Boolean reservaPagada;

    private List<CheckInSegmentoResponse> segmentos;

    private String mensaje;
}