package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.VueloOperado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VueloOperadoRepository
        extends JpaRepository<VueloOperado, Integer>,
        JpaSpecificationExecutor<VueloOperado> {

    Optional<VueloOperado> findByVueloProgramadoId(Integer vueloProgramadoId);

    boolean existsByVueloProgramadoId(Integer vueloProgramadoId);
}