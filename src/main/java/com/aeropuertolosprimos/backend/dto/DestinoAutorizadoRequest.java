package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class DestinoAutorizadoRequest {

    private Integer aerolineaId;

    private Integer aeropuertoId;
}