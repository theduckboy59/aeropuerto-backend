CREATE TABLE estado_abordaje_vuelo (
                                       id SERIAL PRIMARY KEY,
                                       nombre VARCHAR(100) NOT NULL,
                                       estado_id INTEGER NOT NULL,
                                       created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT uk_estado_abordaje_vuelo_nombre
                                           UNIQUE (nombre),

                                       CONSTRAINT fk_estado_abordaje_vuelo_estado
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
        'ALTER TABLE estado_abordaje_vuelo ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;

INSERT INTO estado_abordaje_vuelo (nombre)
VALUES
    ('PENDIENTE'),
    ('ABORDADO'),
    ('CANCELADO'),
    ('RECHAZADO');


CREATE TABLE abordaje (
                          id SERIAL PRIMARY KEY,

                          boleto_segmento_id INTEGER NULL,
                          empleado_id INTEGER NULL,
                          puerta_embarque_id INTEGER NULL,
                          estado_abordaje_vuelo_id INTEGER NOT NULL,

                          tipo_abordaje VARCHAR(20) NULL,
                          fecha_abordaje DATE NULL,
                          hora_abordaje TIME NULL,
                          boleto_validado BOOLEAN NULL,

                          fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                          created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_abordaje_boleto_segmento
                              FOREIGN KEY (boleto_segmento_id)
                                  REFERENCES boleto_segmento(id),

                          CONSTRAINT fk_abordaje_empleado
                              FOREIGN KEY (empleado_id)
                                  REFERENCES empleado(id),

                          CONSTRAINT fk_abordaje_puerta_embarque
                              FOREIGN KEY (puerta_embarque_id)
                                  REFERENCES puerta_embarque(id),

                          CONSTRAINT fk_abordaje_estado_abordaje_vuelo
                              FOREIGN KEY (estado_abordaje_vuelo_id)
                                  REFERENCES estado_abordaje_vuelo(id)
);

DO $$
DECLARE
v_pendiente_id INTEGER;
BEGIN
SELECT id
INTO v_pendiente_id
FROM estado_abordaje_vuelo
WHERE UPPER(nombre) = 'PENDIENTE';

IF v_pendiente_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado PENDIENTE en estado_abordaje_vuelo';
END IF;

EXECUTE format(
        'ALTER TABLE abordaje ALTER COLUMN estado_abordaje_vuelo_id SET DEFAULT %s',
        v_pendiente_id
        );
END $$;


CREATE TABLE bitacora_sistema (
                                  id SERIAL PRIMARY KEY,

                                  tabla_afectada VARCHAR(100) NULL,
                                  id_registro_afectado VARCHAR(100) NULL,
                                  accion VARCHAR(50) NULL,
                                  descripcion VARCHAR(255) NULL,
                                  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  user_id INTEGER NULL,

                                  CONSTRAINT fk_bitacora_sistema_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
);

CREATE INDEX idx_abordaje_boleto_segmento_id
    ON abordaje(boleto_segmento_id);

CREATE INDEX idx_abordaje_empleado_id
    ON abordaje(empleado_id);

CREATE INDEX idx_abordaje_puerta_embarque_id
    ON abordaje(puerta_embarque_id);

CREATE INDEX idx_abordaje_estado_abordaje_vuelo_id
    ON abordaje(estado_abordaje_vuelo_id);

CREATE INDEX idx_bitacora_sistema_user_id
    ON bitacora_sistema(user_id);

CREATE INDEX idx_bitacora_sistema_tabla_afectada
    ON bitacora_sistema(tabla_afectada);