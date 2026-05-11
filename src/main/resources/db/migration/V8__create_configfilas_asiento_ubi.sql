CREATE TABLE tipo_asiento (
                              id SERIAL PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO tipo_asiento (nombre)
VALUES
    ('VENTANA'),
    ('PASILLO'),
    ('MEDIO');


CREATE TABLE clase_vuelo (
                             id SERIAL PRIMARY KEY,
                             nombre VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO clase_vuelo (nombre)
VALUES
    ('ECONOMICA'),
    ('EJECUTIVA');


CREATE TABLE config_clase_filas_avion (
                                          id SERIAL PRIMARY KEY,

                                          avion_id INT NOT NULL,

                                          clase_vuelo_id INT NOT NULL,

                                          fila_desde INT NOT NULL,

                                          fila_hasta INT NOT NULL,

                                          activo BOOLEAN DEFAULT TRUE,

                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                          CONSTRAINT fk_config_clase_filas_avion_avion
                                              FOREIGN KEY (avion_id)
                                                  REFERENCES avion(id),

                                          CONSTRAINT fk_config_clase_filas_avion_clase
                                              FOREIGN KEY (clase_vuelo_id)
                                                  REFERENCES clase_vuelo(id)
);


CREATE TABLE asiento_ubi (
                             id SERIAL PRIMARY KEY,

                             avion_id INT NOT NULL,

                             clase_vuelo_id INT NOT NULL,

                             tipo_asiento_id INT NOT NULL,

                             nivel INT NOT NULL,

                             fila INT NOT NULL,

                             columna VARCHAR(2) NOT NULL,

                             numero_asiento VARCHAR(10) NOT NULL,

                             bloque INT,

                             lado VARCHAR(10),

                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_asiento_ubi_avion
                                 FOREIGN KEY (avion_id)
                                     REFERENCES avion(id),

                             CONSTRAINT fk_asiento_ubi_clase
                                 FOREIGN KEY (clase_vuelo_id)
                                     REFERENCES clase_vuelo(id),

                             CONSTRAINT fk_asiento_ubi_tipo_asiento
                                 FOREIGN KEY (tipo_asiento_id)
                                     REFERENCES tipo_asiento(id),

                             CONSTRAINT uq_asiento_ubi_avion_nivel_numero
                                 UNIQUE (avion_id, nivel, numero_asiento),

                             CONSTRAINT uq_asiento_ubi_avion_nivel_fila_columna
                                 UNIQUE (avion_id, nivel, fila, columna)
);


CREATE INDEX idx_config_clase_filas_avion_avion
    ON config_clase_filas_avion(avion_id);

CREATE INDEX idx_config_clase_filas_avion_clase
    ON config_clase_filas_avion(clase_vuelo_id);

CREATE INDEX idx_asiento_ubi_avion
    ON asiento_ubi(avion_id);

CREATE INDEX idx_asiento_ubi_clase
    ON asiento_ubi(clase_vuelo_id);

CREATE INDEX idx_asiento_ubi_tipo_asiento
    ON asiento_ubi(tipo_asiento_id);