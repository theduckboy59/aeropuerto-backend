CREATE TABLE estado_avion (
                              id SERIAL PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO estado_avion (nombre)
VALUES
    ('ASIGNADO'),
    ('MANTENIMIENTO'),
    ('FUERA_SERVICIO'),
    ('DISPONIBLE');


CREATE TABLE avion (
                       id SERIAL PRIMARY KEY,

                       aerolinea_id INT NOT NULL,

                       estado_avion_id INT NOT NULL,

                       modelo_avion_id INT NOT NULL,

                       codigo_avion VARCHAR(50) NOT NULL,

                       numero_serie VARCHAR(100),

                       anio INT NOT NULL,

                       filas_configuradas INT NOT NULL,

                       cantidad_vuelos INT NOT NULL DEFAULT 0,

                       estado_id INT NOT NULL,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT ck_avion_anio
                           CHECK (anio >= 1950),

                       CONSTRAINT ck_avion_filas_configuradas
                           CHECK (filas_configuradas > 0),

                       CONSTRAINT ck_avion_cantidad_vuelos
                           CHECK (cantidad_vuelos >= 0),

                       CONSTRAINT fk_avion_aerolinea
                           FOREIGN KEY (aerolinea_id)
                               REFERENCES aerolinea(id),

                       CONSTRAINT fk_avion_estado_operativo
                           FOREIGN KEY (estado_avion_id)
                               REFERENCES estado_avion(id),

                       CONSTRAINT fk_avion_modelo_avion
                           FOREIGN KEY (modelo_avion_id)
                               REFERENCES modelo_avion(id),

                       CONSTRAINT fk_avion_estado
                           FOREIGN KEY (estado_id)
                               REFERENCES status_catalog(id)
);

DO $$
DECLARE
v_activo_id INTEGER;
BEGIN
SELECT id
INTO v_activo_id
FROM status_catalog
WHERE UPPER(name) = 'ACTIVO';

IF v_activo_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado ACTIVO en status_catalog';
END IF;

EXECUTE format(
        'ALTER TABLE avion ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;