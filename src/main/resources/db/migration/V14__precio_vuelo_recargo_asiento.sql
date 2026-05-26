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

CREATE INDEX idx_precio_vuelo_vuelo_programado_id
    ON precio_vuelo(vuelo_programado_id);

CREATE INDEX idx_precio_vuelo_clase_vuelo_id
    ON precio_vuelo(clase_vuelo_id);

CREATE INDEX idx_recargo_asiento_tipo_vuelo_programado_id
    ON recargo_asiento_tipo(vuelo_programado_id);

CREATE INDEX idx_recargo_asiento_tipo_clase_vuelo_id
    ON recargo_asiento_tipo(clase_vuelo_id);

DO $$
DECLARE
v_vuelo_programado_id INTEGER;
    v_clase_economica_id INTEGER;
    v_clase_ejecutiva_id INTEGER;
BEGIN
SELECT id
INTO v_vuelo_programado_id
FROM vuelo_programado
ORDER BY id
    LIMIT 1;

IF v_vuelo_programado_id IS NULL THEN
        RAISE NOTICE 'No existe ningún vuelo_programado. Se omite inserción de precios y recargos.';
        RETURN;
END IF;

SELECT id
INTO v_clase_economica_id
FROM clase_vuelo
WHERE UPPER(nombre) = 'ECONOMICA'
    LIMIT 1;

IF v_clase_economica_id IS NULL THEN
        RAISE NOTICE 'No existe la clase ECONOMICA. Se omite inserción de precios y recargos.';
        RETURN;
END IF;

SELECT id
INTO v_clase_ejecutiva_id
FROM clase_vuelo
WHERE UPPER(nombre) = 'EJECUTIVA'
    LIMIT 1;

IF v_clase_ejecutiva_id IS NULL THEN
        RAISE NOTICE 'No existe la clase EJECUTIVA. Se omite inserción de precios y recargos.';
        RETURN;
END IF;

INSERT INTO precio_vuelo (
    vuelo_programado_id,
    clase_vuelo_id,
    precio,
    fecha_vigencia_desde,
    fecha_vigencia_hasta
)
VALUES
    (v_vuelo_programado_id, v_clase_economica_id, 100.00, CURRENT_DATE, NULL),
    (v_vuelo_programado_id, v_clase_ejecutiva_id, 200.00, CURRENT_DATE, NULL);

INSERT INTO recargo_asiento_tipo (
    vuelo_programado_id,
    clase_vuelo_id,
    tipo_asiento,
    recargo
)
VALUES
    (v_vuelo_programado_id, v_clase_economica_id, 'VENTANA', 20.00),
    (v_vuelo_programado_id, v_clase_economica_id, 'PASILLO', 10.00),
    (v_vuelo_programado_id, v_clase_economica_id, 'MEDIO', 0.00),
    (v_vuelo_programado_id, v_clase_ejecutiva_id, 'VENTANA', 30.00),
    (v_vuelo_programado_id, v_clase_ejecutiva_id, 'PASILLO', 20.00),
    (v_vuelo_programado_id, v_clase_ejecutiva_id, 'MEDIO', 0.00);
END $$;