package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.dto.AbordajeVueloPendienteResponse;
import com.aeropuertolosprimos.backend.model.Abordaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AbordajeRepository extends JpaRepository<Abordaje, Integer> {

    boolean existsByBoletoSegmentoIdAndEstadoAbordajeVueloId(
            Integer boletoSegmentoId,
            Integer estadoAbordajeVueloId
    );

    @Query("""
            SELECT new com.aeropuertolosprimos.backend.dto.AbordajeVueloPendienteResponse(
                vo.id,
                v.codigoVuelo,
                v.aerolineaId,
                aeropuertoSalida.id,
                aeropuertoSalida.nombre,
                aeropuertoSalida.codigoIata,
                aeropuertoLlegada.id,
                aeropuertoLlegada.nombre,
                aeropuertoLlegada.codigoIata,
                vp.fechaSalida,
                vp.horaSalida,
                ev.nombre
            )
            FROM VueloOperado vo,
                 VueloProgramado vp,
                 Vuelo v,
                 EstadoVuelo ev,
                 Aeropuerto aeropuertoSalida,
                 Aeropuerto aeropuertoLlegada
            WHERE vp.id = vo.vueloProgramadoId
              AND v.id = vp.vueloId
              AND ev.id = vo.estadoVueloId
              AND aeropuertoSalida.id = vp.aeropuertoSalidaId
              AND aeropuertoLlegada.id = vp.aeropuertoLlegadaId
              AND v.estadoId = :estadoActivoId
              AND v.aerolineaId = :aerolineaId
              AND LOWER(ev.nombre) IN :estadosAbordaje
            ORDER BY vp.fechaSalida ASC, vp.horaSalida ASC
            """)
    List<AbordajeVueloPendienteResponse> listarVuelosPendientesParaAbordaje(
            @Param("aerolineaId") Integer aerolineaId,
            @Param("estadosAbordaje") List<String> estadosAbordaje,
            @Param("estadoActivoId") Integer estadoActivoId
    );
}