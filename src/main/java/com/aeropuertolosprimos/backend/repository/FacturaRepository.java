package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    Optional<Factura> findFirstByPagoIdOrderByIdDesc(Integer pagoId);
}