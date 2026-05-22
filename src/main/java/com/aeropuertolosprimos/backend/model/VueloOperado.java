package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vuelo_operado")
public class VueloOperado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vuelo_programado_id")
    private Integer vueloProgramadoId;

    @Column(name = "tipo_segmento_vuelo_id")
    private Integer tipoSegmentoVueloId;

    @Column(name = "estado_vuelo_id")
    private Integer estadoVueloId;

    @Column(name = "cantidad_segmentos")
    private Integer cantidadSegmentos;

    @Column(name = "segmento_actual_orden")
    private Integer segmentoActualOrden;

    @Column(name = "tuvo_escala")
    private Boolean tuvoEscala;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.tipoSegmentoVueloId == null) {
            this.tipoSegmentoVueloId = 1;
        }

        if (this.estadoVueloId == null) {
            this.estadoVueloId = 1;
        }

        if (this.cantidadSegmentos == null) {
            this.cantidadSegmentos = 1;
        }

        if (this.segmentoActualOrden == null) {
            this.segmentoActualOrden = 1;
        }

        if (this.tuvoEscala == null) {
            this.tuvoEscala = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}