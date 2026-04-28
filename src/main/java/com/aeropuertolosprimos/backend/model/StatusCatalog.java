package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data // Genera automáticamente métodos comunes (get/set, equals, hashCode)
@Entity // Marca la clase como entidad JPA (tabla en BD)
@Table(name = "status_catalog") // Nombre de la tabla (catálogo de estados)
public class StatusCatalog { // Definición de la entidad

    @Id // Define la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincremental en BD
    private Integer id; // Identificador único del estado

    private String name; // Nombre del estado (ej: ACTIVO / INACTIVO)
}