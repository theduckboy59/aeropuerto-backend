package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

@Data
public class ConfigClaseFilasAvionSugerenciaResponse {

    private Integer avionId;

    private Integer filasConfiguradas;

    private String claseBase;

    private Integer filaDesdeBase;

    private Integer filaHastaBase;

    private String claseSugerida;

    private Integer filaDesdeSugerida;

    private Integer filaHastaSugerida;

    private String mensaje;
}