CREATE TABLE precio_vuelo (
                              id SERIAL PRIMARY KEY,
                              vuelo_programado_id INTEGER NULL,
                              clase_vuelo_id INTEGER NULL,
                              precio DECIMAL(10,2) NULL,
                              fecha_vigencia_desde DATE NULL,
                              fecha_vigencia_hasta DATE NULL,
                              created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_precio_vuelo_vuelo_programado
                                  FOREIGN KEY (vuelo_programado_id)
                                      REFERENCES vuelo_programado(id),

                              CONSTRAINT fk_precio_vuelo_clase_vuelo
                                  FOREIGN KEY (clase_vuelo_id)
                                      REFERENCES clase_vuelo(id)
);

CREATE TABLE recargo_asiento_tipo (
                                      id SERIAL PRIMARY KEY,
                                      vuelo_programado_id INTEGER NULL,
                                      clase_vuelo_id INTEGER NULL,
                                      tipo_asiento VARCHAR(20) NULL,
                                      recargo DECIMAL(10,2) NULL DEFAULT 0,
                                      fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_recargo_asiento_tipo_vuelo_programado
                                          FOREIGN KEY (vuelo_programado_id)
                                              REFERENCES vuelo_programado(id),

                                      CONSTRAINT fk_recargo_asiento_tipo_clase_vuelo
                                          FOREIGN KEY (clase_vuelo_id)
                                              REFERENCES clase_vuelo(id)
);

CREATE INDEX idx_precio_vuelo_vuelo_programado_id ON precio_vuelo(vuelo_programado_id);
CREATE INDEX idx_precio_vuelo_clase_vuelo_id ON precio_vuelo(clase_vuelo_id);
CREATE INDEX idx_recargo_asiento_tipo_vuelo_programado_id ON recargo_asiento_tipo(vuelo_programado_id);
CREATE INDEX idx_recargo_asiento_tipo_clase_vuelo_id ON recargo_asiento_tipo(clase_vuelo_id);

INSERT INTO precio_vuelo (
    id,
    vuelo_programado_id,
    clase_vuelo_id,
    precio,
    fecha_vigencia_desde,
    fecha_vigencia_hasta
)
VALUES
    (1, 1, 1, 100.00, CURRENT_DATE, NULL),
    (2, 1, 2, 200.00, CURRENT_DATE, NULL);

SELECT setval(
               pg_get_serial_sequence('precio_vuelo', 'id'),
               (SELECT MAX(id) FROM precio_vuelo)
       );

INSERT INTO recargo_asiento_tipo (
    id,
    vuelo_programado_id,
    clase_vuelo_id,
    tipo_asiento,
    recargo
)
VALUES
    (1, 1, 1, 'VENTANA', 20.00),
    (2, 1, 1, 'PASILLO', 10.00),
    (3, 1, 1, 'MEDIO', 0.00),
    (4, 1, 2, 'VENTANA', 30.00),
    (5, 1, 2, 'PASILLO', 20.00),
    (6, 1, 2, 'MEDIO', 0.00);

SELECT setval(
               pg_get_serial_sequence('recargo_asiento_tipo', 'id'),
               (SELECT MAX(id) FROM recargo_asiento_tipo)
       );