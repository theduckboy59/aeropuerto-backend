/* ============================================================
   V11 - ASIENTO VUELO
   Requiere tablas existentes:
   - vuelo_operado
   - asiento_ubi
   ============================================================ */


/* ============================================================
   CATÁLOGO: ESTADO ASIENTO
   ============================================================ */

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


/* ============================================================
   ASIENTO VUELO
   Disponibilidad de un asiento físico en un vuelo operado.
   ============================================================ */

CREATE TABLE asiento_vuelo (
                               id SERIAL PRIMARY KEY,

                               vuelo_operado_id INT NULL,

                               asiento_ubi_id INT NULL,

                               estado_asiento_id INT NULL,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_asiento_vuelo_vuelo_operado
                                   FOREIGN KEY (vuelo_operado_id)
                                       REFERENCES vuelo_operado(id),

                               CONSTRAINT fk_asiento_vuelo_asiento_ubi
                                   FOREIGN KEY (asiento_ubi_id)
                                       REFERENCES asiento_ubi(id),

                               CONSTRAINT fk_asiento_vuelo_estado_asiento
                                   FOREIGN KEY (estado_asiento_id)
                                       REFERENCES estado_asiento(id),

                               CONSTRAINT uk_asiento_vuelo_operado_asiento
                                   UNIQUE (vuelo_operado_id, asiento_ubi_id)
);

CREATE INDEX idx_asiento_vuelo_vuelo_operado_id
    ON asiento_vuelo(vuelo_operado_id);

CREATE INDEX idx_asiento_vuelo_asiento_ubi_id
    ON asiento_vuelo(asiento_ubi_id);

CREATE INDEX idx_asiento_vuelo_estado_asiento_id
    ON asiento_vuelo(estado_asiento_id);


/* ============================================================
   INSERT INICIAL SEGURO
   Genera disponibilidad para vuelos_operados existentes.
   Solo toma asientos vendibles:
   - asiento_ubi.clase_vuelo_id IS NOT NULL
   ============================================================ */

INSERT INTO asiento_vuelo (
    vuelo_operado_id,
    asiento_ubi_id,
    estado_asiento_id
)
SELECT
    vo.id,
    au.id,
    1
FROM vuelo_operado vo
         JOIN asiento_ubi au
              ON au.avion_id = vo.avion_id
WHERE au.clase_vuelo_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM asiento_vuelo avu
    WHERE avu.vuelo_operado_id = vo.id
      AND avu.asiento_ubi_id = au.id
);