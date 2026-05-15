// EstadoAvionCatalogService.java
package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.model.EstadoAvion;
import com.aeropuertolosprimos.backend.repository.EstadoAvionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EstadoAvionCatalogService {

    private final EstadoAvionRepository estadoAvionRepository;
    private final Map<String, Integer> estadoNombreToId = new ConcurrentHashMap<>();
    private final Map<Integer, String> estadoIdToNombre = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadCatalog() {
        refreshCatalog();
    }

    public void refreshCatalog() {
        estadoNombreToId.clear();
        estadoIdToNombre.clear();

        estadoAvionRepository.findAll().forEach(estado -> {
            estadoNombreToId.put(estado.getNombre().toUpperCase(), estado.getId());
            estadoIdToNombre.put(estado.getId(), estado.getNombre());
        });
    }

    public Integer getIdByNombre(String nombre) {
        Integer id = estadoNombreToId.get(nombre.toUpperCase());
        if (id == null) {
            throw new RuntimeException("Estado de avión no encontrado: " + nombre);
        }
        return id;
    }

    public String getNombreById(Integer id) {
        String nombre = estadoIdToNombre.get(id);
        if (nombre == null) {
            throw new RuntimeException("ID de estado de avión no encontrado: " + id);
        }
        return nombre;
    }

    public boolean puedeModificarse(Integer estadoAvionId) {
        String nombre = getNombreById(estadoAvionId);
        // Solo MANTENIMIENTO y FUERA_SERVICIO pueden modificarse
        return "MANTENIMIENTO".equals(nombre) || "FUERA_SERVICIO".equals(nombre);
    }
}