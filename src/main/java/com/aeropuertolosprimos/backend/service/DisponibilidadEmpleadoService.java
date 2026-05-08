package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.DisponibilidadEmpleadoRequest;
import com.aeropuertolosprimos.backend.dto.DisponibilidadEmpleadoResponse;

import java.util.List;

public interface DisponibilidadEmpleadoService {

    DisponibilidadEmpleadoResponse crear(
            DisponibilidadEmpleadoRequest request
    );

    DisponibilidadEmpleadoResponse actualizar(
            Integer id,
            DisponibilidadEmpleadoRequest request
    );

    DisponibilidadEmpleadoResponse obtenerPorEmpleado(
            Integer empleadoId
    );

    List<DisponibilidadEmpleadoResponse> listar();
}