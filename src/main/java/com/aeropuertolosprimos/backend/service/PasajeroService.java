package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.model.Pasajero;

import java.util.List;

public interface PasajeroService {

    Pasajero crear(Pasajero pasajero);

    List<Pasajero> listar();

    Pasajero obtenerPorId(Integer id);

    void eliminar(Integer id);
}