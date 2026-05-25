package com.aeropuertolosprimos.backend.service;

public interface CatalogoEstadoService {

    Integer obtenerActivoId();

    Integer obtenerInactivoId();

    Integer obtenerEstadoIdPorNombre(String nombre);
}