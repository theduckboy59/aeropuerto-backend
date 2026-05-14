package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AeropuertoRequest {

    private String nombre;

    private String pais;

    private String ciudad;

    private List<PuertaEmbarqueRequest> puertas;
}