package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.CheckInRequest;
import com.aeropuertolosprimos.backend.dto.CheckInResponse;

public interface CheckInService {

    CheckInResponse realizar(
            CheckInRequest request
    );

    CheckInResponse consultarPorBoleto(
            Integer boletoId
    );

    CheckInResponse consultarPorPase(
            String codigoPaseAbordar
    );
}