package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByReservaIdOrderByIdDesc(Integer reservaId);

    Optional<Pago> findFirstByReservaIdAndEstadoPagoIdOrderByIdDesc(
            Integer reservaId,
            Integer estadoPagoId
    );

    Optional<Pago> findFirstByReservaIdAndRecargoEquipajeAndEstadoPagoIdOrderByIdDesc(
            Integer reservaId,
            BigDecimal recargoEquipaje,
            Integer estadoPagoId
    );

    Optional<Pago> findFirstByReservaIdAndRecargoEquipajeGreaterThanAndEstadoPagoIdOrderByIdDesc(
            Integer reservaId,
            BigDecimal recargoEquipaje,
            Integer estadoPagoId
    );
}