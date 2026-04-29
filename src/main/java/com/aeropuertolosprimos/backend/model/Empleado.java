package com.aeropuertolosprimos.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "tipo_empleado_id")
    private Integer tipoEmpleadoId;

    @Column(name = "aerolinea_id")
    private Integer aerolineaId;

    @Column(name = "codigo_empleado")
    private String codigoEmpleado;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "turno_id")
    private Integer turnoId;

    @Column(name = "nivel_acceso_id")
    private Integer nivelAccesoId;

    @Column(name = "rol_id")
    private Integer rolId;

    @Column(name = "area_id")
    private Integer areaId;

    @Column(name = "licencia_id")
    private Integer licenciaId;

    @Column(name = "fecha_vencimiento_licencia")
    private LocalDate fechaVencimientoLicencia;

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