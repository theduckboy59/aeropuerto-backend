package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.repository.StatusCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogoEstadoServiceImpl implements CatalogoEstadoService {

    private static final String ACTIVO = "ACTIVO";
    private static final String INACTIVO = "INACTIVO";

    private final StatusCatalogRepository statusCatalogRepository;

    @Override
    public Integer obtenerActivoId() {
        return obtenerEstadoIdPorNombre(ACTIVO);
    }

    @Override
    public Integer obtenerInactivoId() {
        return obtenerEstadoIdPorNombre(INACTIVO);
    }

    @Override
    public Integer obtenerEstadoIdPorNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new BusinessException("Debe ingresar el nombre del estado");
        }

        return statusCatalogRepository
                .findByNameIgnoreCase(nombre.trim())
                .orElseThrow(() ->
                        new BusinessException("Estado no encontrado en status_catalog: " + nombre)
                )
                .getId();
    }
}