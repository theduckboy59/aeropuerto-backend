package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ReservaSegmentoAsientoRequest {

    private Integer segmentoOperadoId;

    private Integer asientoVueloId;
}