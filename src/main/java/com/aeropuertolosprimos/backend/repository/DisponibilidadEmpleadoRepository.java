package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.DisponibilidadEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisponibilidadEmpleadoRepository
        extends JpaRepository<DisponibilidadEmpleado, Integer> {

    Optional<DisponibilidadEmpleado> findByEmpleadoId(
            Integer empleadoId
    );

    List<DisponibilidadEmpleado> findByDisponible(
            Boolean disponible
    );
}