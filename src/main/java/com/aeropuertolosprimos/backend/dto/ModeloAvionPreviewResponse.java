package com.aeropuertolosprimos.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class  ModeloAvionPreviewResponse {

    private List<SeatLevel> niveles;

    private Integer totalColumnas;

    private Integer pasillos;
}