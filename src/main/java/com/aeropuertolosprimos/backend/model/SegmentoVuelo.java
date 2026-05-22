package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "segmento_vuelo")
public class SegmentoVuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vuelo_programado_id")
    private Integer vueloProgramadoId;

    @Column(name = "orden_segmento")
    private Integer ordenSegmento;

    @Column(name = "aeropuerto_salida_id")
    private Integer aeropuertoSalidaId;

    @Column(name = "aeropuerto_llegada_id")
    private Integer aeropuertoLlegadaId;

    @Column(name = "tipo_segmento_vuelo_id")
    private Integer tipoSegmentoVueloId;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "fecha_llegada")
    private LocalDate fechaLlegada;

    @Column(name = "hora_llegada")
    private LocalTime horaLlegada;

    @Column(name = "estado_id")
    private Integer estadoId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.estadoId == null) {
            this.estadoId = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}