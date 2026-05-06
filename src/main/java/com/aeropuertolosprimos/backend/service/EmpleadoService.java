package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.dto.EmpleadoResponse;

import java.time.LocalDate;
import java.util.List;

public interface EmpleadoService {

    EmpleadoResponse crear(EmpleadoRequest request);

    List<EmpleadoResponse> listar(
            Integer tipoEmpleadoId,
            Integer aerolineaId,
            LocalDate fechaIngreso,
            LocalDate fechaSalida,
            Integer turnoId,
            Integer rolId,
            Integer nivelAccesoId,
            Integer areaId
    );

    EmpleadoResponse obtenerPorId(Integer id);

    EmpleadoResponse actualizar(Integer id, EmpleadoRequest request);

    void eliminar(Integer id);
}