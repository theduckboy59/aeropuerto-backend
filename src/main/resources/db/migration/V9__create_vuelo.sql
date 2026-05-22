/* ============================================================
   V9 - VUELOS + VUELOS PROGRAMADOS
   Requiere tablas existentes:
   - status_catalog
   - aerolinea
   - aeropuerto
   - puerta_embarque
   - destino_autorizado
   ============================================================ */


/* ============================================================
   CATÁLOGO FUTURO: ESTADO VUELO
   Se usará después en vuelo_operado.
   ============================================================ */

CREATE TABLE estado_vuelo (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_estado_vuelo_nombre UNIQUE (nombre),
    CONSTRAINT chk_estado_vuelo_nombre_no_vacio
        CHECK (BTRIM(nombre) <> '')
);

INSERT INTO estado_vuelo (id, nombre)
VALUES
    (1, 'PROGRAMADO'),
    (2, 'ABORDANDO'),
    (3, 'EN_VUELO'),
    (4, 'ATERRIZADO'),
    (5, 'RETRASADO'),
    (6, 'CANCELADO'),
    (7, 'EN_ESCALA'),
    (8, 'FINALIZADO');

SELECT setval(
               pg_get_serial_sequence('estado_vuelo', 'id'),
               (SELECT MAX(id) FROM estado_vuelo)
       );

/* ============================================================
   CATÁLOGO FUTURO: TIPO SEGMENTO VUELO
   Se usará después si se agregan segmentos/paradas.
   No se relaciona todavía con vuelo_programado.
   ============================================================ */

CREATE TABLE tipo_segmento_vuelo (
                                     id SERIAL PRIMARY KEY,

                                     nombre VARCHAR(100) NOT NULL,

                                     requiere_nuevo_asiento BOOLEAN NOT NULL DEFAULT FALSE,
                                     permite_embarque BOOLEAN NOT NULL DEFAULT FALSE,
                                     detiene_flujo_si_cancela BOOLEAN NOT NULL DEFAULT TRUE,

                                     estado_id INT NOT NULL DEFAULT 1,

                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT uk_tipo_segmento_vuelo_nombre UNIQUE (nombre),

                                     CONSTRAINT fk_tipo_segmento_vuelo_estado
                                         FOREIGN KEY (estado_id)
                                             REFERENCES status_catalog(id),

                                     CONSTRAINT chk_tipo_segmento_vuelo_nombre_no_vacio
                                         CHECK (BTRIM(nombre) <> '')
);

CREATE INDEX idx_tipo_segmento_vuelo_estado_id
    ON tipo_segmento_vuelo(estado_id);

INSERT INTO tipo_segmento_vuelo (
    id,
    nombre,
    requiere_nuevo_asiento,
    permite_embarque,
    detiene_flujo_si_cancela,
    estado_id
)
VALUES
    (1, 'DIRECTO', FALSE, FALSE, TRUE, 1),
    (2, 'TECNICO', FALSE, FALSE, TRUE, 1),
    (3, 'CAMBIO_AVION', TRUE, TRUE, TRUE, 1);

SELECT setval(
               pg_get_serial_sequence('tipo_segmento_vuelo', 'id'),
               (SELECT MAX(id) FROM tipo_segmento_vuelo)
       );
/* ============================================================
   VUELO
   Cabecera del vuelo.
   Borrado lógico:
   - ACTIVO = status_catalog.id 1
   - INACTIVO = status_catalog.id 2
   ============================================================ */

CREATE TABLE vuelo (
                       id SERIAL PRIMARY KEY,

                       aerolinea_id INT NULL,

                       codigo_vuelo VARCHAR(100) NULL,

                       estado_id INT NULL,

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

CREATE INDEX idx_vuelo_aerolinea_id
    ON vuelo(aerolinea_id);

CREATE INDEX idx_vuelo_estado_id
    ON vuelo(estado_id);


/* ============================================================
   VUELO PROGRAMADO
   Planificación del vuelo.
   Las puertas de embarque NO son FK.
   Se guardan como código porque salen del aeropuerto.
   Ejemplo:
   aeropuerto_salida_id = 1
   puerta_embarque_salida = 'A1'
   ============================================================ */

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