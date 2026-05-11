package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "config_clase_filas_avion")
public class ConfigClaseFilasAvion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "avion_id")
    private Integer avionId;

    @Column(name = "clase_vuelo_id")
    private Integer claseVueloId;

    @Column(name = "fila_desde")
    private Integer filaDesde;

    @Column(name = "fila_hasta")
    private Integer filaHasta;

    private Boolean activo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}