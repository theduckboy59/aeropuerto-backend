package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmpleadoRepository extends
        JpaRepository<Empleado, Integer>,
        JpaSpecificationExecutor<Empleado> {

    Optional<Empleado> findByCodigoEmpleado(String codigoEmpleado);

    long countByTipoEmpleadoId(Integer tipoEmpleadoId);
}