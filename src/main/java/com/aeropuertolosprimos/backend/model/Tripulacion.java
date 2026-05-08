package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tripulacion")
public class Tripulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;

    @Column(name = "aerolinea_id")
    private Integer aerolineaId;

    @Column(name = "estado_tripulacion_id")
    private Integer estadoTripulacionId;

    @Column(name = "created_at")
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