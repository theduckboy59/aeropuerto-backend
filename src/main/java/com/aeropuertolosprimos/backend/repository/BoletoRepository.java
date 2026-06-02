package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.Boleto;
import org.springframework.data.jpa.repository.JpaRepository;
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
