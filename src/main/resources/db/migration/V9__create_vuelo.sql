CREATE TABLE estado_vuelo (
                              id SERIAL PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uk_estado_vuelo_nombre UNIQUE (nombre),
                              CONSTRAINT chk_estado_vuelo_nombre_no_vacio
                                  CHECK (BTRIM(nombre) <> '')
);

INSERT INTO estado_vuelo (nombre)
VALUES
    ('PROGRAMADO'),
    ('ABORDANDO'),
    ('EN_VUELO'),
    ('ATERRIZADO'),
    ('RETRASADO'),
    ('CANCELADO'),
    ('EN_ESCALA'),
    ('FINALIZADO');


CREATE TABLE tipo_segmento_vuelo (
                                     id SERIAL PRIMARY KEY,

                                     nombre VARCHAR(100) NOT NULL,

                                     requiere_nuevo_asiento BOOLEAN NOT NULL DEFAULT FALSE,
                                     permite_embarque BOOLEAN NOT NULL DEFAULT FALSE,
                                     detiene_flujo_si_cancela BOOLEAN NOT NULL DEFAULT TRUE,

                                     estado_id INT NOT NULL,

                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT uk_tipo_segmento_vuelo_nombre UNIQUE (nombre),

                                     CONSTRAINT fk_tipo_segmento_vuelo_estado
                                         FOREIGN KEY (estado_id)
                                             REFERENCES status_catalog(id),

                                     CONSTRAINT chk_tipo_segmento_vuelo_nombre_no_vacio
                                         CHECK (BTRIM(nombre) <> '')
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
        'ALTER TABLE tipo_segmento_vuelo ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;

CREATE INDEX idx_tipo_segmento_vuelo_estado_id
    ON tipo_segmento_vuelo(estado_id);

INSERT INTO tipo_segmento_vuelo (
    nombre,
    requiere_nuevo_asiento,
    permite_embarque,
    detiene_flujo_si_cancela
)
VALUES
    ('DIRECTO', FALSE, FALSE, TRUE),
    ('TECNICO', FALSE, FALSE, TRUE),
    ('CAMBIO_AVION', TRUE, TRUE, TRUE);



CREATE TABLE vuelo (
                       id SERIAL PRIMARY KEY,

                       aerolinea_id INT NULL,

                       codigo_vuelo VARCHAR(100) NULL,

                       estado_id INT NOT NULL,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_vuelo_codigo_vuelo
                           UNIQUE (codigo_vuelo),

                       CONSTRAINT chk_vuelo_codigo_vuelo_no_vacio
                           CHECK (
                               codigo_vuelo IS NULL
                                   OR BTRIM(codigo_vuelo) <> ''
                               ),

                       CONSTRAINT fk_vuelo_aerolinea
                           FOREIGN KEY (aerolinea_id)
                               REFERENCES aerolinea(id),

                       CONSTRAINT fk_vuelo_estado
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
        'ALTER TABLE vuelo ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;

CREATE INDEX idx_vuelo_aerolinea_id
    ON vuelo(aerolinea_id);

CREATE INDEX idx_vuelo_estado_id
    ON vuelo(estado_id);


CREATE TABLE vuelo_programado (
                                  id SERIAL PRIMARY KEY,

                                  vuelo_id INT NULL,

                                  aeropuerto_salida_id INT NULL,

                                  aeropuerto_llegada_id INT NULL,

                                  puerta_embarque_salida VARCHAR(10) NULL,

                                  puerta_embarque_llegada VARCHAR(10) NULL,

                                  fecha_salida DATE NULL,

                                  hora_salida TIME NULL,

                                  fecha_llegada DATE NULL,

                                  hora_llegada TIME NULL,

                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_vuelo_programado_vuelo
                                      FOREIGN KEY (vuelo_id)
                                          REFERENCES vuelo(id),

                                  CONSTRAINT fk_vuelo_programado_aeropuerto_salida
                                      FOREIGN KEY (aeropuerto_salida_id)
                                          REFERENCES aeropuerto(id),

                                  CONSTRAINT fk_vuelo_programado_aeropuerto_llegada
                                      FOREIGN KEY (aeropuerto_llegada_id)
                                          REFERENCES aeropuerto(id),

                                  CONSTRAINT chk_vuelo_programado_aeropuertos_diferentes
                                      CHECK (
                                          aeropuerto_salida_id IS NULL
                                              OR aeropuerto_llegada_id IS NULL
                                              OR aeropuerto_salida_id <> aeropuerto_llegada_id
                                          ),

                                  CONSTRAINT chk_vuelo_programado_puerta_salida_no_vacia
                                      CHECK (
                                          puerta_embarque_salida IS NULL
                                              OR BTRIM(puerta_embarque_salida) <> ''
                                          ),

                                  CONSTRAINT chk_vuelo_programado_puerta_llegada_no_vacia
                                      CHECK (
                                          puerta_embarque_llegada IS NULL
                                              OR BTRIM(puerta_embarque_llegada) <> ''
                                          ),

                                  CONSTRAINT chk_vuelo_programado_llegada_mayor_salida
                                      CHECK (
                                          fecha_salida IS NULL
                                              OR hora_salida IS NULL
                                              OR fecha_llegada IS NULL
                                              OR hora_llegada IS NULL
                                              OR (fecha_llegada, hora_llegada) > (fecha_salida, hora_salida)
                                          )
);

CREATE INDEX idx_vuelo_programado_vuelo_id
    ON vuelo_programado(vuelo_id);

CREATE INDEX idx_vuelo_programado_aeropuerto_salida_id
    ON vuelo_programado(aeropuerto_salida_id);

CREATE INDEX idx_vuelo_programado_aeropuerto_llegada_id
    ON vuelo_programado(aeropuerto_llegada_id);

CREATE INDEX idx_vuelo_programado_puerta_embarque_salida
    ON vuelo_programado(puerta_embarque_salida);

CREATE INDEX idx_vuelo_programado_puerta_embarque_llegada
    ON vuelo_programado(puerta_embarque_llegada);

CREATE INDEX idx_vuelo_programado_fecha_hora_salida
    ON vuelo_programado(fecha_salida, hora_salida);

CREATE INDEX idx_vuelo_programado_fecha_hora_llegada
    ON vuelo_programado(fecha_llegada, hora_llegada);