package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class FinalizarAbordajeResponse {

    private Integer vueloOperadoId;

    private String estadoVuelo;

    private Integer boletosAbordados;

    private Integer boletosCancelados;

    private String mensaje;
}