CREATE TABLE estado_avion (
                              id SERIAL PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO estado_avion (nombre)
VALUES
    ('ACTIVO'),
    ('MANTENIMIENTO'),
    ('FUERA_SERVICIO'),
    ('INACTIVO');


CREATE TABLE avion (
                       id SERIAL PRIMARY KEY,

                       aerolinea_id INT NOT NULL,

                       estado_avion_id INT NOT NULL,

                       modelo_avion_id INT NOT NULL,

                       codigo_avion VARCHAR(50) NOT NULL,

                       numero_serie VARCHAR(100),

                       anio INT NOT NULL,

                       filas_configuradas INT NOT NULL,

                       cantidad_vuelos INT NOT NULL DEFAULT 0,

                       estado_id INT NOT NULL DEFAULT 1,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT ck_avion_anio
                           CHECK (anio >= 1950),

                       CONSTRAINT ck_avion_filas_configuradas
                           CHECK (filas_configuradas > 0),

                       CONSTRAINT ck_avion_cantidad_vuelos
                           CHECK (cantidad_vuelos >= 0),

                       CONSTRAINT fk_avion_aerolinea
                           FOREIGN KEY (aerolinea_id)
                               REFERENCES aerolinea(id),

                       CONSTRAINT fk_avion_estado_operativo
                           FOREIGN KEY (estado_avion_id)
                               REFERENCES estado_avion(id),

                       CONSTRAINT fk_avion_modelo_avion
                           FOREIGN KEY (modelo_avion_id)
                               REFERENCES modelo_avion(id),

                       CONSTRAINT fk_avion_estado
                           FOREIGN KEY (estado_id)
                               REFERENCES status_catalog(id)
);

INSERT INTO avion
(
    aerolinea_id,
    estado_avion_id,
    modelo_avion_id,
    codigo_avion,
    numero_serie,
    anio,
    filas_configuradas,
    cantidad_vuelos,
    estado_id
)
VALUES
-- AEROLÍNEA 1 - AVIANCA / BOEING
(
    1,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Boeing' AND codigo_modelo = '737-800'),
    'AV-BOE-738-001',
    'SN-BOE738-2020-001',
    2020,
    30,
    0,
    1
),
(
    1,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Boeing' AND codigo_modelo = '737-700'),
    'AV-BOE-737-002',
    'SN-BOE737-2018-002',
    2018,
    25,
    0,
    1
),
(
    1,
    (SELECT id FROM estado_avion WHERE nombre = 'MANTENIMIENTO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Boeing' AND codigo_modelo = '757-200'),
    'AV-BOE-757-003',
    'SN-BOE757-2016-003',
    2016,
    32,
    12,
    1
),
(
    1,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Boeing' AND codigo_modelo = '787-8'),
    'AV-BOE-788-004',
    'SN-BOE788-2021-004',
    2021,
    42,
    0,
    1
),

-- AEROLÍNEA 2 - COPA AIRLINES / AIRBUS
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Airbus' AND codigo_modelo = 'A320'),
    'CM-AIR-A320-001',
    'SN-A320-2019-001',
    2019,
    32,
    0,
    1
),
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Airbus' AND codigo_modelo = 'A321'),
    'CM-AIR-A321-002',
    'SN-A321-2022-002',
    2022,
    42,
    0,
    1
),
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'MANTENIMIENTO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Airbus' AND codigo_modelo = 'A330-200'),
    'CM-AIR-A330-003',
    'SN-A330-2017-003',
    2017,
    45,
    8,
    1
),
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Airbus' AND codigo_modelo = 'A350-900'),
    'CM-AIR-A350-004',
    'SN-A350-2023-004',
    2023,
    50,
    0,
    1
),

-- REGIONALES REPARTIDOS ENTRE AVIANCA Y COPA
(
    1,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Embraer' AND codigo_modelo = 'E190'),
    'AV-EMB-E190-001',
    'SN-E190-2018-001',
    2018,
    30,
    0,
    1
),
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Embraer' AND codigo_modelo = 'E195'),
    'CM-EMB-E195-002',
    'SN-E195-2020-002',
    2020,
    34,
    0,
    1
),
(
    1,
    (SELECT id FROM estado_avion WHERE nombre = 'FUERA_SERVICIO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Bombardier' AND codigo_modelo = 'CRJ-900'),
    'AV-BOM-CRJ9-003',
    'SN-CRJ900-2015-003',
    2015,
    28,
    25,
    1
),
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'ATR' AND codigo_modelo = 'ATR-72'),
    'CM-ATR-072-004',
    'SN-ATR72-2021-004',
    2021,
    20,
    0,
    1
),

-- AVIONES PEQUEÑOS / PRIVADOS
(
    1,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Cessna' AND codigo_modelo = '208-Caravan'),
    'AV-CES-208-001',
    'SN-C208-2019-001',
    2019,
    8,
    0,
    1
),
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'ACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Gulfstream' AND codigo_modelo = 'G650'),
    'CM-GUL-G650-001',
    'SN-G650-2022-001',
    2022,
    12,
    0,
    1
),
(
    2,
    (SELECT id FROM estado_avion WHERE nombre = 'INACTIVO'),
    (SELECT id FROM modelo_avion WHERE fabricante = 'Beechcraft' AND codigo_modelo = 'King-Air-350'),
    'CM-BEE-KA350-001',
    'SN-KA350-2014-001',
    2014,
    10,
    40,
    2
);