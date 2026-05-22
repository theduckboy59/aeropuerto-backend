/* ============================================================
   V11 - ASIENTO VUELO POR SEGMENTO OPERADO
   Requiere tablas existentes:
   - segmento_operado
   - asiento_ubi
   ============================================================ */


/* ============================================================
   CATÁLOGO: ESTADO ASIENTO
   ============================================================ */

CREATE TABLE estado_asiento (
                                id SERIAL PRIMARY KEY,

                                nombre VARCHAR(100) NOT NULL,

                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT uk_estado_asiento_nombre
                                    UNIQUE (nombre),

                                CONSTRAINT chk_estado_asiento_nombre_no_vacio
                                    CHECK (BTRIM(nombre) <> '')
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


/* ============================================================
   ASIENTO VUELO
   Disponibilidad de asiento físico por segmento operado.
   ============================================================ */

CREATE TABLE asiento_vuelo (
                               id SERIAL PRIMARY KEY,

                               segmento_operado_id INT NOT NULL,

                               asiento_ubi_id INT NOT NULL,

                               estado_asiento_id INT NOT NULL DEFAULT 1,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_asiento_vuelo_segmento_operado
                                   FOREIGN KEY (segmento_operado_id)
                                       REFERENCES segmento_operado(id),

                               CONSTRAINT fk_asiento_vuelo_asiento_ubi
                                   FOREIGN KEY (asiento_ubi_id)
                                       REFERENCES asiento_ubi(id),

                               CONSTRAINT fk_asiento_vuelo_estado_asiento
                                   FOREIGN KEY (estado_asiento_id)
                                       REFERENCES estado_asiento(id),

                               CONSTRAINT uk_asiento_vuelo_segmento_asiento
                                   UNIQUE (segmento_operado_id, asiento_ubi_id)
);

CREATE INDEX idx_asiento_vuelo_segmento_operado_id
    ON asiento_vuelo(segmento_operado_id);

CREATE INDEX idx_asiento_vuelo_asiento_ubi_id
    ON asiento_vuelo(asiento_ubi_id);

CREATE INDEX idx_asiento_vuelo_estado_asiento_id
    ON asiento_vuelo(estado_asiento_id);