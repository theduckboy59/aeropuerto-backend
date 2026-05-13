package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "asiento_ubi")
public class AsientoUbi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "avion_id")
    private Integer avionId;

    @Column(name = "clase_vuelo_id")
    private Integer claseVueloId;

    @Column(name = "tipo_asiento_id")
    private Integer tipoAsientoId;

    private Integer nivel;

    private Integer fila;

    private String columna;

    @Column(name = "numero_asiento")
    private String numeroAsiento;

    private Integer bloque;

    private String lado;

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