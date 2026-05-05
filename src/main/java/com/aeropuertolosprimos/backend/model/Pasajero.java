package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pasajero")
public class Pasajero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "dpi", nullable = false, unique = true)
    private String dpi;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "nacionalidad", nullable = false)
    private String nacionalidad;

    @Column(name = "codigo_area", nullable = false)
    private String codigoArea;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;

    @Column(name = "direccion", nullable = false)
    private String direccion;

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

        if (this.estadoId == null) {
            this.estadoId = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}