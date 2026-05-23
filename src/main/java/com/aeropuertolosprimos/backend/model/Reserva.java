package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_reserva")
    private String codigoReserva;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "pasajero_id")
    private Integer pasajeroId;

    @Column(name = "vuelo_operado_id")
    private Integer vueloOperadoId;

    @Column(name = "estado_reserva_id")
    private Integer estadoReservaId;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    private BigDecimal subtotal;

    @Column(name = "recargo_total")
    private BigDecimal recargoTotal;

    private BigDecimal total;

    private String observaciones;

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

        if (fechaReserva == null) fechaReserva = LocalDateTime.now();
        if (estadoId == null) estadoId = 1;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}