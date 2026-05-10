CREATE TABLE aeropuerto (
                            id SERIAL PRIMARY KEY,
                            nombre VARCHAR(150) NOT NULL UNIQUE,
                            codigo_iata VARCHAR(3),
                            codigo_icao VARCHAR(4),
                            pais VARCHAR(100) NOT NULL,
                            ciudad VARCHAR(100) NOT NULL,
                            estado_id INT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_aeropuerto_estado
                                FOREIGN KEY (estado_id)
                                    REFERENCES status_catalog(id)
);

CREATE TABLE puerta_embarque (
                                 id SERIAL PRIMARY KEY,
                                 aeropuerto_id INT NOT NULL,
                                 codigo VARCHAR(10) NOT NULL,
                                 estado_id INT,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_puerta_embarque_aeropuerto
                                     FOREIGN KEY (aeropuerto_id)
                                         REFERENCES aeropuerto(id),

                                 CONSTRAINT fk_puerta_embarque_estado
                                     FOREIGN KEY (estado_id)
                                         REFERENCES status_catalog(id),

                                 CONSTRAINT uq_puerta_aeropuerto_codigo
                                     UNIQUE (aeropuerto_id, codigo)
);

CREATE TABLE destino_autorizado (
                                    id SERIAL PRIMARY KEY,
                                    aerolinea_id INT NOT NULL,
                                    aeropuerto_id INT NOT NULL,
                                    fecha_autorizacion DATE DEFAULT CURRENT_DATE,
                                    estado_id INT,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_destino_autorizado_aerolinea
                                        FOREIGN KEY (aerolinea_id)
                                            REFERENCES aerolinea(id),

                                    CONSTRAINT fk_destino_autorizado_aeropuerto
                                        FOREIGN KEY (aeropuerto_id)
                                            REFERENCES aeropuerto(id),

                                    CONSTRAINT fk_destino_autorizado_estado
                                        FOREIGN KEY (estado_id)
                                            REFERENCES status_catalog(id),

                                    CONSTRAINT uq_destino_autorizado
                                        UNIQUE (aerolinea_id, aeropuerto_id)
);

INSERT INTO aeropuerto (
    nombre,
    codigo_iata,
    codigo_icao,
    pais,
    ciudad,
    estado_id
)
VALUES
    ('La Aurora', 'GUA', 'MGGT', 'Guatemala', 'Ciudad de Guatemala', 1),

    ('Tocumen', 'PTY', 'MPTO', 'Panamá', 'Ciudad de Panamá', 1),

    ('Juan Santamaria', 'SJO', 'MROC', 'Costa Rica', 'San José', 1);

INSERT INTO puerta_embarque (
    aeropuerto_id,
    codigo,
    estado_id
)
VALUES
    (1, 'A1', 1),
    (1, 'A2', 1),

    (2, 'B1', 1),
    (2, 'B2', 1),

    (3, 'C1', 1),
    (3, 'C2', 1);

INSERT INTO destino_autorizado (
    aerolinea_id,
    aeropuerto_id,
    estado_id
)
VALUES
    (1, 1, 1),
    (1, 2, 1),

    (2, 2, 1),
    (2, 3, 1);