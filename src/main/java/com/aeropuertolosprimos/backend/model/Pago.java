package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reserva_id")
    private Integer reservaId;

    @Column(name = "metodo_pago_id")
    private Integer metodoPagoId;

    private BigDecimal monto;

    @Column(name = "recargo_equipaje")
    private BigDecimal recargoEquipaje;

    @Column(name = "estado_pago_id")
    private Integer estadoPagoId;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }

        if (recargoEquipaje == null) {
            recargoEquipaje = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
}