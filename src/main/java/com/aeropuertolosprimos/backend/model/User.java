package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data // Genera automáticamente métodos comunes (get/set, equals, hashCode)
@Entity // Indica que esta clase es una entidad JPA (tabla en BD)
@Table(name = "users") // Nombre de la tabla en la base de datos
public class User { // Definición de la entidad User

    @Id // Marca este campo como clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincremental gestionado por la BD
    private Integer id; // Identificador único del usuario

    private String username; // Nombre de usuario (longitud/control definido en BD)

    private String email; // Correo electrónico (unique/validaciones en BD)

    private String password; // Contraseña (debe almacenarse encriptada)

    @Column(name = "estado_id", nullable = false) // Mapea el atributo a la columna estado_id
    private Integer estadoId; // FK manejada manualmente (sin relación JPA)

    @Column(name = "created_at", updatable = false) // Mapea a columna de fecha de creación
    private LocalDateTime createdAt; // Fecha de creación del registro

    @Column(name = "updated_at") // Mapea a columna de última actualización
    private LocalDateTime updatedAt; // Fecha de modificación del registro

    @PrePersist // Se ejecuta antes de insertar en BD
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.estadoId == null) { // Asigna ACTIVO por defecto
            this.estadoId = 1;
        }
    }

    @PreUpdate // Se ejecuta antes de actualizar en BD
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}