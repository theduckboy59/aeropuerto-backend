package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.BoletoAsiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletoAsientoRepository extends JpaRepository<BoletoAsiento, Integer> {

    List<BoletoAsiento> findByBoletoSegmentoId(
            Integer boletoSegmentoId
    );
}