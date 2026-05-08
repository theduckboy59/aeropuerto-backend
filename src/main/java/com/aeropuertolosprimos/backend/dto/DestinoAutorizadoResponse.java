package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DestinoAutorizadoResponse {

    private Integer id;

    private Integer aerolineaId;

    private String aerolineaNombre;

    private Integer aeropuertoId;

    private String aeropuertoNombre;

    private String pais;

    private Integer estadoId;

    private LocalDate fechaAutorizacion;
}