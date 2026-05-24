package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {

    Optional<CheckIn> findFirstByBoletoSegmentoIdAndEstadoCheckinIdOrderByIdDesc(
            Integer boletoSegmentoId,
            Integer estadoCheckinId
    );

    List<CheckIn> findByBoletoSegmentoIdOrderByIdDesc(
            Integer boletoSegmentoId
    );

    boolean existsByBoletoSegmentoIdAndEstadoCheckinId(
            Integer boletoSegmentoId,
            Integer estadoCheckinId
    );
}