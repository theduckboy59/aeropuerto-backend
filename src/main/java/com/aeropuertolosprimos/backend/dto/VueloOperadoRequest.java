package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class VueloOperadoRequest {

    private Integer vueloProgramadoId;

    private Integer tipoSegmentoVueloId;

    private Integer cantidadSegmentos;

    private List<VueloOperadoSegmentoRequest> segmentos;
}