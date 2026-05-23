package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "boleto_asiento")
public class BoletoAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "boleto_segmento_id")
    private Integer boletoSegmentoId;

    @Column(name = "asiento_vuelo_id")
    private Integer asientoVueloId;

    @Column(name = "clase_vuelo_id")
    private Integer claseVueloId;

    @Column(name = "estado_id")
    private Integer estadoId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (estadoId == null) estadoId = 1;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}