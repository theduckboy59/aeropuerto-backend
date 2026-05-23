package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Equipaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipajeRepository extends JpaRepository<Equipaje, Integer> {

    List<Equipaje> findByBoletoId(
            Integer boletoId
    );

    List<Equipaje> findByBoletoIdOrderByNumeroMaletaAsc(
            Integer boletoId
    );
}