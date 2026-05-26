CREATE TABLE aeropuerto (
                            id SERIAL PRIMARY KEY,
                            nombre VARCHAR(150) NOT NULL UNIQUE,
                            codigo_iata VARCHAR(3),
                            codigo_icao VARCHAR(4),
                            pais VARCHAR(100) NOT NULL,
                            ciudad VARCHAR(100) NOT NULL,
                            estado_id INT NOT NULL,
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
                                 estado_id INT NOT NULL,
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
                                    estado_id INT NOT NULL,
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
        'ALTER TABLE aeropuerto ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );

EXECUTE format(
        'ALTER TABLE puerta_embarque ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );

EXECUTE format(
        'ALTER TABLE destino_autorizado ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;