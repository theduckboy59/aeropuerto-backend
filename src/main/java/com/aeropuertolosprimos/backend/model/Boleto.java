package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "boleto")
public class Boleto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_boleto")
    private String codigoBoleto;

    @Column(name = "codigo_pase_abordar")
    private String codigoPaseAbordar;

    @Column(name = "reserva_id")
    private Integer reservaId;

    @Column(name = "pasajero_id")
    private Integer pasajeroId;

    @Column(name = "vuelo_operado_id")
    private Integer vueloOperadoId;

    @Column(name = "estado_boleto_id")
    private Integer estadoBoletoId;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "precio_base")
    private BigDecimal precioBase;

    @Column(name = "recargo_equipaje")
    private BigDecimal recargoEquipaje;

    private BigDecimal total;

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

        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}