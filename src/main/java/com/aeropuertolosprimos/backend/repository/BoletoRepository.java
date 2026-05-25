package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Boleto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BoletoRepository extends JpaRepository<Boleto, Integer> {

    Optional<Boleto> findFirstByReservaIdOrderByIdAsc(
            Integer reservaId
    );

    List<Boleto> findByReservaIdOrderByIdAsc(
            Integer reservaId
    );

    Optional<Boleto> findFirstByPasajeroIdAndVueloOperadoIdAndEstadoIdOrderByIdAsc(
            Integer pasajeroId,
            Integer vueloOperadoId,
            Integer estadoId
    );

    Optional<Boleto> findFirstByPasajeroIdAndVueloOperadoIdAndEstadoBoletoIdAndEstadoIdOrderByIdDesc(
            Integer pasajeroId,
            Integer vueloOperadoId,
            Integer estadoBoletoId,
            Integer estadoId
    );

    List<Boleto> findByVueloOperadoIdAndEstadoId(
            Integer vueloOperadoId,
            Integer estadoId
    );

    @Query("""
        SELECT COUNT(b)
        FROM Boleto b, BoletoSegmento bs, SegmentoOperado so, SegmentoVuelo sv
        WHERE b.id = bs.boletoId
          AND bs.segmentoOperadoId = so.id
          AND so.segmentoVueloId = sv.id
          AND b.pasajeroId = :pasajeroId
          AND b.estadoId = :estadoActivoId
          AND (:estadoCanceladoId IS NULL OR b.estadoBoletoId <> :estadoCanceladoId)
          AND sv.fechaSalida = :fechaSalida
          AND sv.horaSalida = :horaSalida
        """)
    long countBoletosPasajeroMismaFechaHora(
            @Param("pasajeroId") Integer pasajeroId,
            @Param("fechaSalida") LocalDate fechaSalida,
            @Param("horaSalida") LocalTime horaSalida,
            @Param("estadoActivoId") Integer estadoActivoId,
            @Param("estadoCanceladoId") Integer estadoCanceladoId
    );

    Optional<Boleto> findFirstByCodigoPaseAbordarAndEstadoIdOrderByIdDesc(
            String codigoPaseAbordar,
            Integer estadoId
    );

    Optional<Boleto> findFirstByPasajeroIdAndVueloOperadoIdAndEstadoBoletoIdNotAndEstadoIdOrderByIdDesc(
            Integer pasajeroId,
            Integer vueloOperadoId,
            Integer estadoBoletoId,
            Integer estadoId
    );
}