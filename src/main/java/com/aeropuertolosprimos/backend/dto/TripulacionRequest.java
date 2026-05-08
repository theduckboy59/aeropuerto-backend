package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class TripulacionRequest {

    private Integer aerolineaId;

    private Integer pilotoId;

    private Integer copilotoId;

    private Integer ingenieroId;

    private List<Integer> tripulantesCabinaIds;
}