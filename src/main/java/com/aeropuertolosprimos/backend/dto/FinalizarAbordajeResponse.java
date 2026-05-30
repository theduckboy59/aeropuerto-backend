package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class FinalizarAbordajeResponse {

    private Integer vueloOperadoId;

    private Integer segmentoOperadoId;

    private Integer ordenSegmento;

    private Integer segmentoActualOrden;

    private Integer cantidadSegmentos;

    private String estadoVuelo;

    private Integer boletosAbordados;

    private Integer boletosCancelados;

    private String mensaje;
}