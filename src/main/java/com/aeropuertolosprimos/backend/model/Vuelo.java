package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vuelo")
public class Vuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "aerolinea_id")
    private Integer aerolineaId;

    @Column(name = "codigo_vuelo")
    private String codigoVuelo;

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

        if (this.codigoVuelo != null) {
            this.codigoVuelo = this.codigoVuelo.trim().toUpperCase();
        }
    }

    @PreUpdate
    public void preUpdate() {

        this.updatedAt = LocalDateTime.now();

        if (this.codigoVuelo != null) {
            this.codigoVuelo = this.codigoVuelo.trim().toUpperCase();
        }
    }
}