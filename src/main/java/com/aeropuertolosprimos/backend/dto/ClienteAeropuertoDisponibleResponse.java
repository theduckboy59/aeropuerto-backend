package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ClienteAeropuertoDisponibleResponse {

    private Integer aeropuertoId;

    private String nombre;

    private String codigoIata;

    private String codigoIcao;

    private String pais;

    private String ciudad;

    private Integer totalVuelos;

    private Integer asientosDisponiblesTotal;
}