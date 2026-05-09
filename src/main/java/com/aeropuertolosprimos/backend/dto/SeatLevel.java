package com.aeropuertolosprimos.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SeatLevel {

    private Integer nivel;

    private List<List<String>> bloques;
}