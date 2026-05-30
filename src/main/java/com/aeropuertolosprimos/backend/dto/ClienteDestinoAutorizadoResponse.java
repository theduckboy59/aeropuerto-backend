package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ClienteDestinoAutorizadoResponse {

    private Integer aeropuertoId;

    private String nombre;

    private String codigoIata;

    private String ciudad;

    private String pais;
}