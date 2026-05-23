package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.HistorialBoleto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialBoletoRepository extends JpaRepository<HistorialBoleto, Integer> {

    List<HistorialBoleto> findByBoletoIdOrderByIdDesc(Integer boletoId);
}