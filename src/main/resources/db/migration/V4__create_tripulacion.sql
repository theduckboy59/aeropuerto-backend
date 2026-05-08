CREATE TABLE disponibilidad_empleado (
                                         id SERIAL PRIMARY KEY,
                                         empleado_id INT NOT NULL,
                                         fecha DATE,
                                         hora_inicio TIME,
                                         hora_fin TIME,
                                         disponible BOOLEAN DEFAULT true,

                                         CONSTRAINT fk_disponibilidad_empleado
                                             FOREIGN KEY (empleado_id)
                                                 REFERENCES empleado(id)
);

CREATE TABLE estado_tripulacion (
                                    id SERIAL PRIMARY KEY,
                                    nombre VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO estado_tripulacion (nombre)
VALUES
    ('DISPONIBLE'),
    ('ASIGNADA'),
    ('INACTIVA');

CREATE TABLE tripulacion (
                             id SERIAL PRIMARY KEY,
                             codigo VARCHAR(20) UNIQUE NOT NULL,
                             aerolinea_id INT,
                             estado_tripulacion_id INT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_tripulacion_aerolinea
                                 FOREIGN KEY (aerolinea_id)
                                     REFERENCES aerolinea(id),

                             CONSTRAINT fk_tripulacion_estado
                                 FOREIGN KEY (estado_tripulacion_id)
                                     REFERENCES estado_tripulacion(id)
);

CREATE TABLE tripulacion_detalle (
                                     id SERIAL PRIMARY KEY,
                                     tripulacion_id INT NOT NULL,
                                     empleado_id INT NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_tripulacion_detalle_tripulacion
                                         FOREIGN KEY (tripulacion_id)
                                             REFERENCES tripulacion(id),

                                     CONSTRAINT fk_tripulacion_detalle_empleado
                                         FOREIGN KEY (empleado_id)
                                             REFERENCES empleado(id),

                                     CONSTRAINT uq_tripulacion_empleado
                                         UNIQUE (tripulacion_id, empleado_id)
);

CREATE OR REPLACE FUNCTION fn_crear_disponibilidad_empleado()
RETURNS TRIGGER AS
$$
BEGIN

INSERT INTO disponibilidad_empleado (
    empleado_id,
    disponible
)
VALUES (
           NEW.id,
           true
       );

RETURN NEW;

END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_crear_disponibilidad_empleado
    AFTER INSERT ON empleado
    FOR EACH ROW
    EXECUTE FUNCTION fn_crear_disponibilidad_empleado();