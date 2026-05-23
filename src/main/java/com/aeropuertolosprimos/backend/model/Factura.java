package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pago_id")
    private Integer pagoId;

    private String nit;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    private String serie;

    private String numero;

    @Column(name = "uuid_fel")
    private String uuidFel;

    @Column(name = "estado_fel")
    private String estadoFel;

    @Column(name = "fecha_factura")
    private LocalDateTime fechaFactura;

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

        if (fechaFactura == null) {
            fechaFactura = LocalDateTime.now();
        }

        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
}