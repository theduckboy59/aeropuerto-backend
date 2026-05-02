package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.model.Empleado;

import java.util.List;

public interface EmpleadoService {

    Empleado crear(EmpleadoRequest request);

    List<Empleado> listar();

    Empleado obtenerPorId(Integer id);

    Empleado actualizar(Integer id, EmpleadoRequest request);

    void eliminar(Integer id);
}