package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class AerolineaRequest {

    private String nombre;

    private String codigoIata;

    private String codigoIcao;

    private String pais;
}