package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.BoletoSegmento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletoSegmentoRepository extends JpaRepository<BoletoSegmento, Integer> {

    List<BoletoSegmento> findByBoletoIdOrderByOrdenSegmentoAsc(
            Integer boletoId
    );
}