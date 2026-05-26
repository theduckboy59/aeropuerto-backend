package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import org.hibernate.annotations.DynamicInsert;

@Data
@Entity
@DynamicInsert
@Table(name = "tipo_segmento_vuelo")
public class TipoSegmentoVuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    @Column(name = "requiere_nuevo_asiento")
    private Boolean requiereNuevoAsiento;

    @Column(name = "permite_embarque")
    private Boolean permiteEmbarque;

    @Column(name = "detiene_flujo_si_cancela")
    private Boolean detieneFlujoSiCancela;

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

        if (this.requiereNuevoAsiento == null) {
            this.requiereNuevoAsiento = false;
        }

        if (this.permiteEmbarque == null) {
            this.permiteEmbarque = false;
        }

        if (this.detieneFlujoSiCancela == null) {
            this.detieneFlujoSiCancela = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}