package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.VueloProgramado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface VueloProgramadoRepository extends
        JpaRepository<VueloProgramado, Integer>,
        JpaSpecificationExecutor<VueloProgramado> {

    Optional<VueloProgramado> findByVueloId(
            Integer vueloId
    );

    @Query("""
            select count(vp)
            from VueloProgramado vp, Vuelo v
            where v.id = vp.vueloId
              and v.estadoId = :estadoActivoId
              and v.aerolineaId = :aerolineaId
              and vp.aeropuertoSalidaId = :aeropuertoSalidaId
              and vp.aeropuertoLlegadaId = :aeropuertoLlegadaId
              and vp.fechaSalida = :fechaSalida
              and vp.horaSalida = :horaSalida
              and vp.fechaLlegada = :fechaLlegada
              and vp.horaLlegada = :horaLlegada
              and (:vueloIdExcluir is null or v.id <> :vueloIdExcluir)
            """)
    long countProgramacionActivaDuplicada(
            @Param("aerolineaId") Integer aerolineaId,
            @Param("aeropuertoSalidaId") Integer aeropuertoSalidaId,
            @Param("aeropuertoLlegadaId") Integer aeropuertoLlegadaId,
            @Param("fechaSalida") LocalDate fechaSalida,
            @Param("horaSalida") LocalTime horaSalida,
            @Param("fechaLlegada") LocalDate fechaLlegada,
            @Param("horaLlegada") LocalTime horaLlegada,
            @Param("estadoActivoId") Integer estadoActivoId,
            @Param("vueloIdExcluir") Integer vueloIdExcluir
    );
}