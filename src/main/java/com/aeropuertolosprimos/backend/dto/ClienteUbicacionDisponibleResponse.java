package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ClienteUbicacionDisponibleResponse {

    private String pais;

    private String ciudad;

    private Integer totalAeropuertos;

    private Integer totalVuelos;
}