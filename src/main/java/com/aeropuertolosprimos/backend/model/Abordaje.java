package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "abordaje")
public class Abordaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "boleto_segmento_id")
    private Integer boletoSegmentoId;

    @Column(name = "empleado_id")
    private Integer empleadoId;

    @Column(name = "puerta_embarque_id")
    private Integer puertaEmbarqueId;

    @Column(name = "estado_abordaje_vuelo_id")
    private Integer estadoAbordajeVueloId;

    @Column(name = "tipo_abordaje")
    private String tipoAbordaje;

    @Column(name = "fecha_abordaje")
    private LocalDate fechaAbordaje;

    @Column(name = "hora_abordaje")
    private LocalTime horaAbordaje;

    @Column(name = "boleto_validado")
    private Boolean boletoValidado;

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
        fechaActualizacion = LocalDateTime.now();

        if (fechaAbordaje == null) {
            fechaAbordaje = LocalDate.now();
        }

        if (horaAbordaje == null) {
            horaAbordaje = LocalTime.now();
        }

        if (boletoValidado == null) {
            boletoValidado = true;
        }

        if (tipoAbordaje == null || tipoAbordaje.isBlank()) {
            tipoAbordaje = "MANUAL";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
}