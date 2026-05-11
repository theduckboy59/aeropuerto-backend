package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "avion")
public class Avion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "aerolinea_id")
    private Integer aerolineaId;

    @Column(name = "estado_avion_id")
    private Integer estadoAvionId;

    @Column(name = "modelo_avion_id")
    private Integer modeloAvionId;

    @Column(name = "codigo_avion")
    private String codigoAvion;

    @Column(name = "numero_serie")
    private String numeroSerie;

    private Integer anio;

    @Column(name = "filas_configuradas")
    private Integer filasConfiguradas;

    @Column(name = "cantidad_vuelos")
    private Integer cantidadVuelos;

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

        if (this.cantidadVuelos == null) {
            this.cantidadVuelos = 0;
        }

        if (this.estadoId == null) {
            this.estadoId = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}