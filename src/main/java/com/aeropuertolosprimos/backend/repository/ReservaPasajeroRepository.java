package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.ReservaPasajero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaPasajeroRepository extends JpaRepository<ReservaPasajero, Integer> {

    List<ReservaPasajero> findByPasajeroIdAndEstadoIdOrderByIdDesc(
            Integer pasajeroId,
            Integer estadoId
    );
}