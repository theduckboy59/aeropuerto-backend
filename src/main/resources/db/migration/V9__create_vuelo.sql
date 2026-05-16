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


CREATE TABLE vuelo_programado (
                                  id SERIAL PRIMARY KEY,

                                  vuelo_id INT NULL,

                                  aeropuerto_salida_id INT NULL,

                                  aeropuerto_llegada_id INT NULL,

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
                                          )
);

CREATE INDEX idx_vuelo_programado_vuelo_id
    ON vuelo_programado(vuelo_id);

CREATE INDEX idx_vuelo_programado_aeropuerto_salida_id
    ON vuelo_programado(aeropuerto_salida_id);

CREATE INDEX idx_vuelo_programado_aeropuerto_llegada_id
    ON vuelo_programado(aeropuerto_llegada_id);

CREATE INDEX idx_vuelo_programado_fecha_hora_salida
    ON vuelo_programado(fecha_salida, hora_salida);