package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    Optional<Empleado> findByCodigoEmpleado(String codigoEmpleado);
}