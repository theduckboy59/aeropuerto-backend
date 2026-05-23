CREATE TABLE estado_asiento (
                                id SERIAL PRIMARY KEY,

                                nombre VARCHAR(100) NULL,

                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT uk_estado_asiento_nombre
                                    UNIQUE (nombre),

                                CONSTRAINT chk_estado_asiento_nombre_no_vacio
                                    CHECK (
                                        nombre IS NULL
                                            OR BTRIM(nombre) <> ''
                                        )
);

INSERT INTO estado_asiento (id, nombre)
VALUES
    (1, 'DISPONIBLE'),
    (2, 'RESERVADO'),
    (3, 'OCUPADO'),
    (4, 'BLOQUEADO');

SELECT setval(
               pg_get_serial_sequence('estado_asiento', 'id'),
               (SELECT MAX(id) FROM estado_asiento)
       );


CREATE TABLE asiento_vuelo (
                               id SERIAL PRIMARY KEY,

                               segmento_operado_id INT NULL,

                               codigo_asiento_sistema VARCHAR(80) NULL,

                               estado_asiento_id INT NULL DEFAULT 1,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_asiento_vuelo_segmento_operado
                                   FOREIGN KEY (segmento_operado_id)
                                       REFERENCES segmento_operado(id),

                               CONSTRAINT fk_asiento_vuelo_estado_asiento
                                   FOREIGN KEY (estado_asiento_id)
                                       REFERENCES estado_asiento(id),

                               CONSTRAINT uk_asiento_vuelo_segmento_codigo
                                   UNIQUE (segmento_operado_id, codigo_asiento_sistema)
);

CREATE INDEX idx_asiento_vuelo_segmento_operado_id
    ON asiento_vuelo(segmento_operado_id);

CREATE INDEX idx_asiento_vuelo_codigo_asiento_sistema
    ON asiento_vuelo(codigo_asiento_sistema);

CREATE INDEX idx_asiento_vuelo_estado_asiento_id
    ON asiento_vuelo(estado_asiento_id);