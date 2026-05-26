package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Data
@Entity
@DynamicInsert
@Table(name = "asiento_vuelo")
public class AsientoVuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "segmento_operado_id")
    private Integer segmentoOperadoId;

    @Column(name = "codigo_asiento_sistema")
    private String codigoAsientoSistema;

    @Column(name = "estado_asiento_id")
    private Integer estadoAsientoId;

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