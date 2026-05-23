package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "equipaje")
public class Equipaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "boleto_id")
    private Integer boletoId;

    @Column(name = "pasajero_id")
    private Integer pasajeroId;

    @Column(name = "segmento_operado_id")
    private Integer segmentoOperadoId;

    @Column(name = "tipo_equipaje_id")
    private Integer tipoEquipajeId;

    @Column(name = "estado_equipaje_id")
    private Integer estadoEquipajeId;

    @Column(name = "numero_maleta")
    private Integer numeroMaleta;

    private String descripcion;

    private BigDecimal peso;

    @Column(name = "monto_recargo")
    private BigDecimal montoRecargo;

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