/* ============================================================
   V10 - VUELO OPERADO
   Requiere tablas existentes:
   - vuelo_programado
   - avion
   - tripulacion
   - estado_vuelo
   ============================================================ */

CREATE TABLE vuelo_operado (
                               id SERIAL PRIMARY KEY,

                               vuelo_programado_id INT NULL,

                               avion_id INT NULL,

                               tripulacion_id INT NULL,

                               estado_vuelo_id INT NULL,

                               fecha_salida_real DATE NULL,

                               hora_salida_real TIME NULL,

                               fecha_llegada_real DATE NULL,

                               hora_llegada_real TIME NULL,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_vuelo_operado_vuelo_programado
                                   FOREIGN KEY (vuelo_programado_id)
                                       REFERENCES vuelo_programado(id),

                               CONSTRAINT fk_vuelo_operado_avion
                                   FOREIGN KEY (avion_id)
                                       REFERENCES avion(id),

                               CONSTRAINT fk_vuelo_operado_tripulacion
                                   FOREIGN KEY (tripulacion_id)
                                       REFERENCES tripulacion(id),

                               CONSTRAINT fk_vuelo_operado_estado_vuelo
                                   FOREIGN KEY (estado_vuelo_id)
                                       REFERENCES estado_vuelo(id),

                               CONSTRAINT uk_vuelo_operado_vuelo_programado
                                   UNIQUE (vuelo_programado_id),

                               CONSTRAINT chk_vuelo_operado_llegada_real_mayor_salida_real
                                   CHECK (
                                       fecha_salida_real IS NULL
                                           OR hora_salida_real IS NULL
                                           OR fecha_llegada_real IS NULL
                                           OR hora_llegada_real IS NULL
                                           OR (fecha_llegada_real, hora_llegada_real) > (fecha_salida_real, hora_salida_real)
                                       )
);

CREATE INDEX idx_vuelo_operado_vuelo_programado_id
    ON vuelo_operado(vuelo_programado_id);

CREATE INDEX idx_vuelo_operado_avion_id
    ON vuelo_operado(avion_id);

CREATE INDEX idx_vuelo_operado_tripulacion_id
    ON vuelo_operado(tripulacion_id);

CREATE INDEX idx_vuelo_operado_estado_vuelo_id
    ON vuelo_operado(estado_vuelo_id);

CREATE INDEX idx_vuelo_operado_fecha_hora_salida_real
    ON vuelo_operado(fecha_salida_real, hora_salida_real);

CREATE INDEX idx_vuelo_operado_fecha_hora_llegada_real
    ON vuelo_operado(fecha_llegada_real, hora_llegada_real);

/* ============================================================
   INSERT DE PRUEBA
   Crea un vuelo_operado usando:
   - primer vuelo_programado disponible
   - primer avion existente
   - primera tripulacion existente
   - estado PROGRAMADO
   ============================================================ */

INSERT INTO vuelo_operado (
    vuelo_programado_id,
    avion_id,
    tripulacion_id,
    estado_vuelo_id,
    fecha_salida_real,
    hora_salida_real,
    fecha_llegada_real,
    hora_llegada_real
)
SELECT
    vp.id,
    av.id,
    tr.id,
    ev.id,
    NULL,
    NULL,
    NULL,
    NULL
FROM vuelo_programado vp
         JOIN vuelo v
              ON v.id = vp.vuelo_id
         CROSS JOIN LATERAL (
    SELECT id
    FROM avion
    ORDER BY id
        LIMIT 1
) av
CROSS JOIN LATERAL (
SELECT id
FROM tripulacion
ORDER BY id
    LIMIT 1
    ) tr
    JOIN estado_vuelo ev
ON ev.nombre = 'PROGRAMADO'
WHERE v.estado_id = 1
  AND NOT EXISTS (
    SELECT 1
    FROM vuelo_operado vo
    WHERE vo.vuelo_programado_id = vp.id
    )
ORDER BY vp.id
    LIMIT 1;