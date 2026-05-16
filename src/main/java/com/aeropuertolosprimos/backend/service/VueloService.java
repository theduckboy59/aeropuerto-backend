package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.VueloRequest;
import com.aeropuertolosprimos.backend.dto.VueloResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;

public interface VueloService {

    Page<VueloResponse> findAll(
            String q,
            String buscarSalida,
            String buscarLlegada,
            Integer aerolineaId,
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida,
            LocalTime horaSalida,
            LocalDate fechaLlegada,
            LocalTime horaLlegada,
            Pageable pageable
    );

    VueloResponse findById(
            Integer id
    );

    VueloResponse findByCodigo(
            String codigoVuelo
    );

    VueloResponse create(
            VueloRequest request
    );

    VueloResponse update(
            Integer id,
            VueloRequest request
    );

    void delete(
            Integer id
    );
}