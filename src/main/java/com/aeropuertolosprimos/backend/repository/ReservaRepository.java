package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByPasajeroIdAndEstadoIdOrderByIdDesc(
            Integer pasajeroId,
            Integer estadoId
    );
}