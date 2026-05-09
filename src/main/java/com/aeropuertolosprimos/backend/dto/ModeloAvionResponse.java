package com.aeropuertolosprimos.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModeloAvionResponse {

    private Integer id;

    private String fabricante;

    private String codigoModelo;

    private String nombre;

    private Integer niveles;

    private Integer pasillos;

    private String configuracion;

    private Integer totalColumnas;

    private Integer filasMin;

    private Integer filasMax;

    private Integer estadoId;
}