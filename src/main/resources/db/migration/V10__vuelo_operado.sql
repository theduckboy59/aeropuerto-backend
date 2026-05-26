CREATE TABLE segmento_vuelo (
                                id SERIAL PRIMARY KEY,

                                vuelo_programado_id INT NOT NULL,

                                orden_segmento INT NOT NULL,

                                aeropuerto_salida_id INT NOT NULL,

                                aeropuerto_llegada_id INT NOT NULL,

                                tipo_segmento_vuelo_id INT NOT NULL,

                                fecha_salida DATE NULL,

                                hora_salida TIME NULL,

                                fecha_llegada DATE NULL,

                                hora_llegada TIME NULL,

                                estado_id INT NOT NULL,

                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_segmento_vuelo_vuelo_programado
                                    FOREIGN KEY (vuelo_programado_id)
                                        REFERENCES vuelo_programado(id),

                                CONSTRAINT fk_segmento_vuelo_aeropuerto_salida
                                    FOREIGN KEY (aeropuerto_salida_id)
                                        REFERENCES aeropuerto(id),

                                CONSTRAINT fk_segmento_vuelo_aeropuerto_llegada
                                    FOREIGN KEY (aeropuerto_llegada_id)
                                        REFERENCES aeropuerto(id),

                                CONSTRAINT fk_segmento_vuelo_tipo_segmento
                                    FOREIGN KEY (tipo_segmento_vuelo_id)
                                        REFERENCES tipo_segmento_vuelo(id),

                                CONSTRAINT fk_segmento_vuelo_estado
                                    FOREIGN KEY (estado_id)
                                        REFERENCES status_catalog(id),

                                CONSTRAINT uk_segmento_vuelo_orden
                                    UNIQUE (vuelo_programado_id, orden_segmento),

                                CONSTRAINT chk_segmento_vuelo_orden
                                    CHECK (orden_segmento BETWEEN 1 AND 3),

                                CONSTRAINT chk_segmento_vuelo_aeropuertos_diferentes
                                    CHECK (aeropuerto_salida_id <> aeropuerto_llegada_id),

                                CONSTRAINT chk_segmento_vuelo_salida_completa
                                    CHECK (
                                        (
                                            fecha_salida IS NULL
                                                AND hora_salida IS NULL
                                            )
                                            OR
                                        (
                                            fecha_salida IS NOT NULL
                                                AND hora_salida IS NOT NULL
                                            )
                                        ),

                                CONSTRAINT chk_segmento_vuelo_llegada_completa
                                    CHECK (
                                        (
                                            fecha_llegada IS NULL
                                                AND hora_llegada IS NULL
                                            )
                                            OR
                                        (
                                            fecha_llegada IS NOT NULL
                                                AND hora_llegada IS NOT NULL
                                            )
                                        ),

                                CONSTRAINT chk_segmento_vuelo_llegada_mayor_salida
                                    CHECK (
                                        fecha_salida IS NULL
                                            OR hora_salida IS NULL
                                            OR fecha_llegada IS NULL
                                            OR hora_llegada IS NULL
                                            OR (fecha_llegada, hora_llegada) > (fecha_salida, hora_salida)
                                        )
);


CREATE TABLE vuelo_operado (
                               id SERIAL PRIMARY KEY,

                               vuelo_programado_id INT NOT NULL,

                               tipo_segmento_vuelo_id INT NOT NULL,

                               estado_vuelo_id INT NOT NULL,

                               cantidad_segmentos INT NOT NULL DEFAULT 1,

                               segmento_actual_orden INT NOT NULL DEFAULT 1,

                               tuvo_escala BOOLEAN NOT NULL DEFAULT FALSE,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_vuelo_operado_vuelo_programado
                                   FOREIGN KEY (vuelo_programado_id)
                                       REFERENCES vuelo_programado(id),

                               CONSTRAINT fk_vuelo_operado_tipo_segmento
                                   FOREIGN KEY (tipo_segmento_vuelo_id)
                                       REFERENCES tipo_segmento_vuelo(id),

                               CONSTRAINT fk_vuelo_operado_estado_vuelo
                                   FOREIGN KEY (estado_vuelo_id)
                                       REFERENCES estado_vuelo(id),

                               CONSTRAINT uk_vuelo_operado_vuelo_programado
                                   UNIQUE (vuelo_programado_id),

                               CONSTRAINT chk_vuelo_operado_cantidad_segmentos
                                   CHECK (cantidad_segmentos BETWEEN 1 AND 3),

                               CONSTRAINT chk_vuelo_operado_segmento_actual
                                   CHECK (
                                       segmento_actual_orden >= 1
                                           AND segmento_actual_orden <= cantidad_segmentos
                                       )
);


CREATE TABLE segmento_operado (
                                  id SERIAL PRIMARY KEY,

                                  vuelo_operado_id INT NOT NULL,

                                  segmento_vuelo_id INT NOT NULL,

                                  orden_segmento INT NOT NULL,

                                  avion_id INT NOT NULL,

                                  tripulacion_id INT NOT NULL,

                                  estado_vuelo_id INT NOT NULL,

                                  fecha_salida_real DATE NULL,

                                  hora_salida_real TIME NULL,

                                  fecha_llegada_real DATE NULL,

                                  hora_llegada_real TIME NULL,

                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_segmento_operado_vuelo_operado
                                      FOREIGN KEY (vuelo_operado_id)
                                          REFERENCES vuelo_operado(id),

                                  CONSTRAINT fk_segmento_operado_segmento_vuelo
                                      FOREIGN KEY (segmento_vuelo_id)
                                          REFERENCES segmento_vuelo(id),

                                  CONSTRAINT fk_segmento_operado_avion
                                      FOREIGN KEY (avion_id)
                                          REFERENCES avion(id),

                                  CONSTRAINT fk_segmento_operado_tripulacion
                                      FOREIGN KEY (tripulacion_id)
                                          REFERENCES tripulacion(id),

                                  CONSTRAINT fk_segmento_operado_estado_vuelo
                                      FOREIGN KEY (estado_vuelo_id)
                                          REFERENCES estado_vuelo(id),

                                  CONSTRAINT uk_segmento_operado_segmento_vuelo
                                      UNIQUE (vuelo_operado_id, segmento_vuelo_id),

                                  CONSTRAINT uk_segmento_operado_orden
                                      UNIQUE (vuelo_operado_id, orden_segmento),

                                  CONSTRAINT chk_segmento_operado_orden
                                      CHECK (orden_segmento BETWEEN 1 AND 3),

                                  CONSTRAINT chk_segmento_operado_salida_real_completa
                                      CHECK (
                                          (
                                              fecha_salida_real IS NULL
                                                  AND hora_salida_real IS NULL
                                              )
                                              OR
                                          (
                                              fecha_salida_real IS NOT NULL
                                                  AND hora_salida_real IS NOT NULL
                                              )
                                          ),

                                  CONSTRAINT chk_segmento_operado_llegada_real_completa
                                      CHECK (
                                          (
                                              fecha_llegada_real IS NULL
                                                  AND hora_llegada_real IS NULL
                                              )
                                              OR
                                          (
                                              fecha_llegada_real IS NOT NULL
                                                  AND hora_llegada_real IS NOT NULL
                                              )
                                          ),

                                  CONSTRAINT chk_segmento_operado_llegada_mayor_salida
                                      CHECK (
                                          fecha_salida_real IS NULL
                                              OR hora_salida_real IS NULL
                                              OR fecha_llegada_real IS NULL
                                              OR hora_llegada_real IS NULL
                                              OR (fecha_llegada_real, hora_llegada_real) > (fecha_salida_real, hora_salida_real)
                                          )
);


DO $$
DECLARE
v_activo_id INTEGER;
    v_programado_id INTEGER;
    v_directo_id INTEGER;
BEGIN
SELECT id
INTO v_activo_id
FROM status_catalog
WHERE UPPER(name) = 'ACTIVO';

IF v_activo_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado ACTIVO en status_catalog';
END IF;

SELECT id
INTO v_programado_id
FROM estado_vuelo
WHERE UPPER(nombre) = 'PROGRAMADO';

IF v_programado_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado PROGRAMADO en estado_vuelo';
END IF;

SELECT id
INTO v_directo_id
FROM tipo_segmento_vuelo
WHERE UPPER(nombre) = 'DIRECTO';

IF v_directo_id IS NULL THEN
        RAISE EXCEPTION 'No existe el tipo DIRECTO en tipo_segmento_vuelo';
END IF;

EXECUTE format(
        'ALTER TABLE segmento_vuelo ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );

EXECUTE format(
        'ALTER TABLE vuelo_operado ALTER COLUMN estado_vuelo_id SET DEFAULT %s',
        v_programado_id
        );

EXECUTE format(
        'ALTER TABLE vuelo_operado ALTER COLUMN tipo_segmento_vuelo_id SET DEFAULT %s',
        v_directo_id
        );

EXECUTE format(
        'ALTER TABLE segmento_operado ALTER COLUMN estado_vuelo_id SET DEFAULT %s',
        v_programado_id
        );
END $$;


CREATE OR REPLACE FUNCTION fn_validar_vuelo_operado_tipo_segmentos()
RETURNS TRIGGER AS
$$
DECLARE
v_tipo_segmento_nombre VARCHAR(100);
BEGIN
SELECT nombre
INTO v_tipo_segmento_nombre
FROM tipo_segmento_vuelo
WHERE id = NEW.tipo_segmento_vuelo_id;

IF v_tipo_segmento_nombre IS NULL THEN
        RAISE EXCEPTION 'Tipo de segmento no encontrado';
END IF;

    IF UPPER(v_tipo_segmento_nombre) = 'DIRECTO'
       AND NEW.cantidad_segmentos <> 1 THEN
        RAISE EXCEPTION 'Un vuelo DIRECTO debe tener exactamente 1 segmento';
END IF;

    IF UPPER(v_tipo_segmento_nombre) IN ('TECNICO', 'CAMBIO_AVION')
       AND NEW.cantidad_segmentos NOT BETWEEN 2 AND 3 THEN
        RAISE EXCEPTION 'Un vuelo con escala debe tener entre 2 y 3 segmentos';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_validar_vuelo_operado_tipo_segmentos
    BEFORE INSERT OR UPDATE ON vuelo_operado
                         FOR EACH ROW
                         EXECUTE FUNCTION fn_validar_vuelo_operado_tipo_segmentos();


CREATE INDEX idx_segmento_vuelo_vuelo_programado_id
    ON segmento_vuelo(vuelo_programado_id);

CREATE INDEX idx_segmento_vuelo_orden
    ON segmento_vuelo(vuelo_programado_id, orden_segmento);

CREATE INDEX idx_segmento_vuelo_tipo_segmento
    ON segmento_vuelo(tipo_segmento_vuelo_id);

CREATE INDEX idx_vuelo_operado_vuelo_programado_id
    ON vuelo_operado(vuelo_programado_id);

CREATE INDEX idx_vuelo_operado_estado_vuelo_id
    ON vuelo_operado(estado_vuelo_id);

CREATE INDEX idx_vuelo_operado_tipo_segmento_vuelo_id
    ON vuelo_operado(tipo_segmento_vuelo_id);

CREATE INDEX idx_segmento_operado_vuelo_operado_id
    ON segmento_operado(vuelo_operado_id);

CREATE INDEX idx_segmento_operado_segmento_vuelo_id
    ON segmento_operado(segmento_vuelo_id);

CREATE INDEX idx_segmento_operado_avion_id
    ON segmento_operado(avion_id);

CREATE INDEX idx_segmento_operado_tripulacion_id
    ON segmento_operado(tripulacion_id);

CREATE INDEX idx_segmento_operado_estado_vuelo_id
    ON segmento_operado(estado_vuelo_id);