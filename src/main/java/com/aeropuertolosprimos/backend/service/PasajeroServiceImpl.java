package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.model.Pasajero;
import com.aeropuertolosprimos.backend.repository.PasajeroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PasajeroServiceImpl implements PasajeroService {

    private final PasajeroRepository repository;

    public PasajeroServiceImpl(PasajeroRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pasajero crear(Pasajero pasajero) {

        if (repository.existsByDpi(pasajero.getDpi())) {
            throw new RuntimeException("Ya existe un pasajero con el mismo DPI");
        }

        return repository.save(pasajero);
    }

    @Override
    public List<Pasajero> listar() {
        return repository.findAll();
    }

    @Override
    public Pasajero obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pasajero no encontrado"));
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}