package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

public @Data class AerolineaResponse {

    private Integer id;

    private String nombre;

    private String codigoIata;

    private String codigoIcao;

    private String pais;

    private Integer estadoId;
}