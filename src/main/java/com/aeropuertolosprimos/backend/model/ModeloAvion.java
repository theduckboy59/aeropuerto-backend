package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "modelo_avion")
public class ModeloAvion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fabricante;

    @Column(name = "codigo_modelo")
    private String codigoModelo;

    private String nombre;

    private Integer niveles;

    private Integer pasillos;

    private String configuracion;

    @Column(name = "total_columnas")
    private Integer totalColumnas;

    @Column(name = "filas_min")
    private Integer filasMin;

    @Column(name = "filas_max")
    private Integer filasMax;

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