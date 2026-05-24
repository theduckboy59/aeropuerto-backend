CREATE TABLE estado_abordaje_vuelo (
                                       id SERIAL PRIMARY KEY,
                                       nombre VARCHAR(100) NULL,
                                       estado_id INTEGER NULL DEFAULT 1,
                                       created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT fk_estado_abordaje_vuelo_estado
                                           FOREIGN KEY (estado_id)
                                               REFERENCES status_catalog(id)
);

CREATE TABLE abordaje (
                          id SERIAL PRIMARY KEY,

                          boleto_segmento_id INTEGER NULL,
                          empleado_id INTEGER NULL,
                          puerta_embarque_id INTEGER NULL,
                          estado_abordaje_vuelo_id INTEGER NULL,

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

INSERT INTO estado_abordaje_vuelo (id, nombre, estado_id)
VALUES
    (1, 'PENDIENTE', 1),
    (2, 'ABORDADO', 1),
    (3, 'CANCELADO', 1),
    (4, 'RECHAZADO', 1);

SELECT setval(
               pg_get_serial_sequence('estado_abordaje_vuelo', 'id'),
               (SELECT MAX(id) FROM estado_abordaje_vuelo)
       );