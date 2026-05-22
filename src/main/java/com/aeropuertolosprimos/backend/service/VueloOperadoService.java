package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.VueloOperadoRequest;
import com.aeropuertolosprimos.backend.dto.VueloOperadoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface VueloOperadoService {

    Page<VueloOperadoResponse> findAll(
            Integer vueloProgramadoId,
            Integer avionId,
            Integer tripulacionId,
            Integer estadoVueloId,
            LocalDate fechaSalidaReal,
            LocalDate fechaLlegadaReal,
            Pageable pageable
    );

    VueloOperadoResponse findById(Integer id);

    VueloOperadoResponse create(VueloOperadoRequest request);

    VueloOperadoResponse update(
            Integer id,
            VueloOperadoRequest request
    );

    VueloOperadoResponse cambiarEstado(
            Integer id,
            Integer estadoVueloId
    );

    void delete(Integer id);
}