package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AsientoVueloResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AsientoVueloService {

    Page<AsientoVueloResponse> findAll(
            Integer vueloOperadoId,
            Integer segmentoOperadoId,
            Integer estadoAsientoId,
            Integer claseVueloId,
            Integer tipoAsientoId,
            Integer nivel,
            Integer fila,
            String columna,
            String numeroAsiento,
            Pageable pageable
    );

    AsientoVueloResponse findById(Integer id);

    void generarAsientosParaVueloOperado(Integer vueloOperadoId);

    void generarAsientosParaSegmentoOperado(Integer segmentoOperadoId);

    AsientoVueloResponse cambiarEstado(
            Integer id,
            Integer estadoAsientoId
    );

    AsientoVueloResponse cambiarEstadoPorNombre(
            Integer id,
            String estadoAsientoNombre
    );
}