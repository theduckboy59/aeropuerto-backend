package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AeropuertoResponse {

    private Integer id;

    private String nombre;

    private String codigoIata;

    private String codigoIcao;

    private String pais;

    private String ciudad;

    private Integer estadoId;

    private List<PuertaEmbarqueResponse> puertas;
}