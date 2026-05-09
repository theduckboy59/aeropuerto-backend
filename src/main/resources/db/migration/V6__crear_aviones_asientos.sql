CREATE TABLE estado_avion (
                              id SERIAL PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO estado_avion (nombre)
VALUES
    ('ACTIVO'),
    ('MANTENIMIENTO'),
    ('FUERA_SERVICIO');

CREATE TABLE clase_vuelo (
                             id SERIAL PRIMARY KEY,
                             nombre VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO clase_vuelo (nombre)
VALUES
    ('ECONOMICA'),
    ('EJECUTIVA'),
    ('PRIMERA_CLASE');

CREATE TABLE estado_asiento (
                                id SERIAL PRIMARY KEY,
                                nombre VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO estado_asiento (nombre)
VALUES
    ('LIBRE'),
    ('RESERVADO'),
    ('ABORDADO'),
    ('BLOQUEADO');

CREATE TABLE modelo_avion (
                              id SERIAL PRIMARY KEY,

                              fabricante VARCHAR(100) NOT NULL,

                              codigo_modelo VARCHAR(50) NOT NULL,

                              nombre VARCHAR(100) NOT NULL,

                              niveles INT NOT NULL,

                              tipo_niveles VARCHAR(20),

                              pasillos INT NOT NULL,

                              configuracion VARCHAR(10),

                              total_columnas INT NOT NULL,

                              filas_min INT NOT NULL,

                              filas_max INT NOT NULL,

                              estado_id INT,

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_modelo_avion_estado
                                  FOREIGN KEY (estado_id)
                                      REFERENCES status_catalog(id)
);

CREATE TABLE avion (
                       id SERIAL PRIMARY KEY,

                       aerolinea_id INT NOT NULL,

                       estado_avion_id INT NOT NULL,

                       modelo_id INT NOT NULL,

                       codigo_avion VARCHAR(50) NOT NULL,

                       anio INT NOT NULL,

                       filas_configuradas INT NOT NULL,

                       cantidad_vuelos INT DEFAULT 0,

                       estado_id INT,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_avion_aerolinea
                           FOREIGN KEY (aerolinea_id)
                               REFERENCES aerolinea(id),

                       CONSTRAINT fk_avion_estado_operativo
                           FOREIGN KEY (estado_avion_id)
                               REFERENCES estado_avion(id),

                       CONSTRAINT fk_avion_modelo
                           FOREIGN KEY (modelo_id)
                               REFERENCES modelo_avion(id),

                       CONSTRAINT fk_avion_estado
                           FOREIGN KEY (estado_id)
                               REFERENCES status_catalog(id)
);

CREATE TABLE config_clase_filas_avion (
                                          id SERIAL PRIMARY KEY,

                                          avion_id INT NOT NULL,

                                          clase_vuelo_id INT NOT NULL,

                                          fila_desde INT NOT NULL,

                                          fila_hasta INT NOT NULL,

                                          activo BOOLEAN DEFAULT TRUE,

                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                          CONSTRAINT fk_config_clase_avion
                                              FOREIGN KEY (avion_id)
                                                  REFERENCES avion(id),

                                          CONSTRAINT fk_config_clase_vuelo
                                              FOREIGN KEY (clase_vuelo_id)
                                                  REFERENCES clase_vuelo(id)
);

CREATE TABLE asiento_ubi (
                             id SERIAL PRIMARY KEY,

                             avion_id INT NOT NULL,

                             clase_vuelo_id INT NOT NULL,

                             nivel INT NOT NULL,

                             fila INT NOT NULL,

                             columna VARCHAR(2) NOT NULL,

                             numero_asiento VARCHAR(10) NOT NULL,

                             bloque INT,

                             lado VARCHAR(10),

                             tipo_asiento VARCHAR(20),

                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_asiento_avion
                                 FOREIGN KEY (avion_id)
                                     REFERENCES avion(id),

                             CONSTRAINT fk_asiento_clase
                                 FOREIGN KEY (clase_vuelo_id)
                                     REFERENCES clase_vuelo(id),

                             CONSTRAINT uq_asiento_avion
                                 UNIQUE (avion_id, numero_asiento)
);

CREATE TABLE asiento_vuelo (
                               id SERIAL PRIMARY KEY,

                               segmento_operado_id INT NOT NULL,

                               asiento_id INT NOT NULL,

                               estado_asiento_id INT NOT NULL,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_asiento_vuelo_asiento
                                   FOREIGN KEY (asiento_id)
                                       REFERENCES asiento_ubi(id),

                               CONSTRAINT fk_asiento_vuelo_estado
                                   FOREIGN KEY (estado_asiento_id)
                                       REFERENCES estado_asiento(id)
);