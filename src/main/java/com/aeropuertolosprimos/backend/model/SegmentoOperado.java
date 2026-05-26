package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.hibernate.annotations.DynamicInsert;

@Data
@Entity
@DynamicInsert
@Table(name = "segmento_operado")
public class SegmentoOperado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vuelo_operado_id")
    private Integer vueloOperadoId;

    @Column(name = "segmento_vuelo_id")
    private Integer segmentoVueloId;

    @Column(name = "orden_segmento")
    private Integer ordenSegmento;

    @Column(name = "avion_id")
    private Integer avionId;

    @Column(name = "tripulacion_id")
    private Integer tripulacionId;

    @Column(name = "estado_vuelo_id")
    private Integer estadoVueloId;

    @Column(name = "fecha_salida_real")
    private LocalDate fechaSalidaReal;

    @Column(name = "hora_salida_real")
    private LocalTime horaSalidaReal;

    @Column(name = "fecha_llegada_real")
    private LocalDate fechaLlegadaReal;

    @Column(name = "hora_llegada_real")
    private LocalTime horaLlegadaReal;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}