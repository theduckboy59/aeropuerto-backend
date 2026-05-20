package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "vuelo_programado")
public class VueloProgramado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vuelo_id")
    private Integer vueloId;

    @Column(name = "aeropuerto_salida_id")
    private Integer aeropuertoSalidaId;

    @Column(name = "aeropuerto_llegada_id")
    private Integer aeropuertoLlegadaId;

    @Column(name = "puerta_embarque_salida")
    private String puertaEmbarqueSalida;

    @Column(name = "puerta_embarque_llegada")
    private String puertaEmbarqueLlegada;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "fecha_llegada")
    private LocalDate fechaLlegada;

    @Column(name = "hora_llegada")
    private LocalTime horaLlegada;

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