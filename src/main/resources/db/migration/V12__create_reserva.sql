CREATE TABLE estado_reserva (
                                id SERIAL PRIMARY KEY,
                                nombre VARCHAR(100) NOT NULL,
                                estado_id INT NOT NULL,
                                created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT uk_estado_reserva_nombre UNIQUE (nombre),
                                CONSTRAINT fk_estado_reserva_estado
                                    FOREIGN KEY (estado_id)
                                        REFERENCES status_catalog(id)
);

CREATE TABLE estado_boleto (
                               id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NOT NULL,
                               estado_id INT NOT NULL,
                               created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uk_estado_boleto_nombre UNIQUE (nombre),
                               CONSTRAINT fk_estado_boleto_estado
                                   FOREIGN KEY (estado_id)
                                       REFERENCES status_catalog(id)
);

CREATE TABLE tipo_equipaje (
                               id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NOT NULL,
                               estado_id INT NOT NULL,
                               created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uk_tipo_equipaje_nombre UNIQUE (nombre),
                               CONSTRAINT fk_tipo_equipaje_estado
                                   FOREIGN KEY (estado_id)
                                       REFERENCES status_catalog(id)
);

CREATE TABLE estado_equipaje (
                                 id SERIAL PRIMARY KEY,
                                 nombre VARCHAR(100) NOT NULL,
                                 estado_id INT NOT NULL,
                                 created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT uk_estado_equipaje_nombre UNIQUE (nombre),
                                 CONSTRAINT fk_estado_equipaje_estado
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
        'ALTER TABLE estado_reserva ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );

EXECUTE format(
        'ALTER TABLE estado_boleto ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );

EXECUTE format(
        'ALTER TABLE tipo_equipaje ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );

EXECUTE format(
        'ALTER TABLE estado_equipaje ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;

INSERT INTO estado_reserva (nombre)
VALUES
    ('CREADA'),
    ('CONFIRMADA'),
    ('CANCELADA');

INSERT INTO estado_boleto (nombre)
VALUES
    ('PENDIENTE_ABORDAR'),
    ('ABORDADO'),
    ('CANCELADO');

INSERT INTO tipo_equipaje (nombre)
VALUES
    ('MALETA'),
    ('MANO');

INSERT INTO estado_equipaje (nombre)
VALUES
    ('REGISTRADO'),
    ('ABORDADO'),
    ('CANCELADO');


CREATE TABLE reserva (
                         id SERIAL PRIMARY KEY,

                         codigo_reserva VARCHAR(100) NULL,

                         user_id INT NULL,
                         pasajero_id INT NULL,
                         vuelo_operado_id INT NULL,

                         estado_reserva_id INT NOT NULL,

                         fecha_reserva TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                         subtotal NUMERIC(12,2) NULL,
                         recargo_total NUMERIC(12,2) NULL,
                         total NUMERIC(12,2) NULL,

                         observaciones VARCHAR(500) NULL,

                         estado_id INT NOT NULL,
                         created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_reserva_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id),

                         CONSTRAINT fk_reserva_pasajero
                             FOREIGN KEY (pasajero_id)
                                 REFERENCES pasajero(id),

                         CONSTRAINT fk_reserva_vuelo_operado
                             FOREIGN KEY (vuelo_operado_id)
                                 REFERENCES vuelo_operado(id),

                         CONSTRAINT fk_reserva_estado_reserva
                             FOREIGN KEY (estado_reserva_id)
                                 REFERENCES estado_reserva(id),

                         CONSTRAINT fk_reserva_estado
                             FOREIGN KEY (estado_id)
                                 REFERENCES status_catalog(id)
);


CREATE TABLE reserva_pasajero (
                                  id SERIAL PRIMARY KEY,

                                  reserva_id INT NULL,
                                  pasajero_id INT NULL,

                                  estado_id INT NOT NULL,
                                  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_reserva_pasajero_reserva
                                      FOREIGN KEY (reserva_id)
                                          REFERENCES reserva(id),

                                  CONSTRAINT fk_reserva_pasajero_pasajero
                                      FOREIGN KEY (pasajero_id)
                                          REFERENCES pasajero(id),

                                  CONSTRAINT fk_reserva_pasajero_estado
                                      FOREIGN KEY (estado_id)
                                          REFERENCES status_catalog(id)
);

CREATE TABLE reserva_segmento (
                                  id SERIAL PRIMARY KEY,

                                  reserva_id INT NULL,
                                  segmento_operado_id INT NULL,

                                  orden_segmento INT NULL,

                                  estado_id INT NOT NULL,
                                  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_reserva_segmento_reserva
                                      FOREIGN KEY (reserva_id)
                                          REFERENCES reserva(id),

                                  CONSTRAINT fk_reserva_segmento_segmento_operado
                                      FOREIGN KEY (segmento_operado_id)
                                          REFERENCES segmento_operado(id),

                                  CONSTRAINT fk_reserva_segmento_estado
                                      FOREIGN KEY (estado_id)
                                          REFERENCES status_catalog(id)
);


CREATE TABLE boleto (
                        id SERIAL PRIMARY KEY,

                        codigo_boleto VARCHAR(100) NULL,
                        codigo_pase_abordar VARCHAR(100) NULL,

                        reserva_id INT NULL,
                        pasajero_id INT NULL,
                        vuelo_operado_id INT NULL,

                        estado_boleto_id INT NOT NULL,

                        fecha_emision TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                        precio_base NUMERIC(12,2) NULL,
                        recargo_equipaje NUMERIC(12,2) NULL,
                        total NUMERIC(12,2) NULL,

                        estado_id INT NOT NULL,
                        created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_boleto_reserva
                            FOREIGN KEY (reserva_id)
                                REFERENCES reserva(id),

                        CONSTRAINT fk_boleto_pasajero
                            FOREIGN KEY (pasajero_id)
                                REFERENCES pasajero(id),

                        CONSTRAINT fk_boleto_vuelo_operado
                            FOREIGN KEY (vuelo_operado_id)
                                REFERENCES vuelo_operado(id),

                        CONSTRAINT fk_boleto_estado_boleto
                            FOREIGN KEY (estado_boleto_id)
                                REFERENCES estado_boleto(id),

                        CONSTRAINT fk_boleto_estado
                            FOREIGN KEY (estado_id)
                                REFERENCES status_catalog(id)
);


CREATE TABLE boleto_segmento (
                                 id SERIAL PRIMARY KEY,

                                 boleto_id INT NULL,
                                 segmento_operado_id INT NULL,

                                 orden_segmento INT NULL,
                                 estado_boleto_id INT NOT NULL,

                                 estado_id INT NOT NULL,
                                 created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_boleto_segmento_boleto
                                     FOREIGN KEY (boleto_id)
                                         REFERENCES boleto(id),

                                 CONSTRAINT fk_boleto_segmento_segmento_operado
                                     FOREIGN KEY (segmento_operado_id)
                                         REFERENCES segmento_operado(id),

                                 CONSTRAINT fk_boleto_segmento_estado_boleto
                                     FOREIGN KEY (estado_boleto_id)
                                         REFERENCES estado_boleto(id),

                                 CONSTRAINT fk_boleto_segmento_estado
                                     FOREIGN KEY (estado_id)
                                         REFERENCES status_catalog(id)
);


CREATE TABLE boleto_asiento (
                                id SERIAL PRIMARY KEY,

                                boleto_segmento_id INT NULL,
                                asiento_vuelo_id INT NULL,
                                clase_vuelo_id INT NULL,

                                estado_id INT NOT NULL,
                                created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_boleto_asiento_boleto_segmento
                                    FOREIGN KEY (boleto_segmento_id)
                                        REFERENCES boleto_segmento(id),

                                CONSTRAINT fk_boleto_asiento_asiento_vuelo
                                    FOREIGN KEY (asiento_vuelo_id)
                                        REFERENCES asiento_vuelo(id),

                                CONSTRAINT fk_boleto_asiento_clase_vuelo
                                    FOREIGN KEY (clase_vuelo_id)
                                        REFERENCES clase_vuelo(id),

                                CONSTRAINT fk_boleto_asiento_estado
                                    FOREIGN KEY (estado_id)
                                        REFERENCES status_catalog(id)
);


CREATE TABLE equipaje (
                          id SERIAL PRIMARY KEY,

                          boleto_id INT NULL,
                          pasajero_id INT NULL,
                          segmento_operado_id INT NULL,

                          tipo_equipaje_id INT NULL,
                          estado_equipaje_id INT NOT NULL,

                          numero_maleta INT NULL,
                          descripcion VARCHAR(255) NULL,

                          peso NUMERIC(10,2) NULL,
                          monto_recargo NUMERIC(12,2) NULL,

                          estado_id INT NOT NULL,
                          created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_equipaje_boleto
                              FOREIGN KEY (boleto_id)
                                  REFERENCES boleto(id),

                          CONSTRAINT fk_equipaje_pasajero
                              FOREIGN KEY (pasajero_id)
                                  REFERENCES pasajero(id),

                          CONSTRAINT fk_equipaje_segmento_operado
                              FOREIGN KEY (segmento_operado_id)
                                  REFERENCES segmento_operado(id),

                          CONSTRAINT fk_equipaje_tipo_equipaje
                              FOREIGN KEY (tipo_equipaje_id)
                                  REFERENCES tipo_equipaje(id),

                          CONSTRAINT fk_equipaje_estado_equipaje
                              FOREIGN KEY (estado_equipaje_id)
                                  REFERENCES estado_equipaje(id),

                          CONSTRAINT fk_equipaje_estado
                              FOREIGN KEY (estado_id)
                                  REFERENCES status_catalog(id)
);


DO $$
DECLARE
v_activo_id INTEGER;
    v_reserva_creada_id INTEGER;
    v_boleto_pendiente_id INTEGER;
    v_equipaje_registrado_id INTEGER;
BEGIN
SELECT id
INTO v_activo_id
FROM status_catalog
WHERE UPPER(name) = 'ACTIVO';

IF v_activo_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado ACTIVO en status_catalog';
END IF;

SELECT id
INTO v_reserva_creada_id
FROM estado_reserva
WHERE UPPER(nombre) = 'CREADA';

IF v_reserva_creada_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado CREADA en estado_reserva';
END IF;

SELECT id
INTO v_boleto_pendiente_id
FROM estado_boleto
WHERE UPPER(nombre) = 'PENDIENTE_ABORDAR';

IF v_boleto_pendiente_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado PENDIENTE_ABORDAR en estado_boleto';
END IF;

SELECT id
INTO v_equipaje_registrado_id
FROM estado_equipaje
WHERE UPPER(nombre) = 'REGISTRADO';

IF v_equipaje_registrado_id IS NULL THEN
        RAISE EXCEPTION 'No existe el estado REGISTRADO en estado_equipaje';
END IF;

EXECUTE format('ALTER TABLE reserva ALTER COLUMN estado_id SET DEFAULT %s', v_activo_id);
EXECUTE format('ALTER TABLE reserva_pasajero ALTER COLUMN estado_id SET DEFAULT %s', v_activo_id);
EXECUTE format('ALTER TABLE reserva_segmento ALTER COLUMN estado_id SET DEFAULT %s', v_activo_id);
EXECUTE format('ALTER TABLE boleto ALTER COLUMN estado_id SET DEFAULT %s', v_activo_id);
EXECUTE format('ALTER TABLE boleto_segmento ALTER COLUMN estado_id SET DEFAULT %s', v_activo_id);
EXECUTE format('ALTER TABLE boleto_asiento ALTER COLUMN estado_id SET DEFAULT %s', v_activo_id);
EXECUTE format('ALTER TABLE equipaje ALTER COLUMN estado_id SET DEFAULT %s', v_activo_id);

EXECUTE format('ALTER TABLE reserva ALTER COLUMN estado_reserva_id SET DEFAULT %s', v_reserva_creada_id);
EXECUTE format('ALTER TABLE boleto ALTER COLUMN estado_boleto_id SET DEFAULT %s', v_boleto_pendiente_id);
EXECUTE format('ALTER TABLE boleto_segmento ALTER COLUMN estado_boleto_id SET DEFAULT %s', v_boleto_pendiente_id);
EXECUTE format('ALTER TABLE equipaje ALTER COLUMN estado_equipaje_id SET DEFAULT %s', v_equipaje_registrado_id);
END $$;


CREATE INDEX idx_reserva_pasajero_id
    ON reserva(pasajero_id);

CREATE INDEX idx_reserva_vuelo_operado_id
    ON reserva(vuelo_operado_id);

CREATE INDEX idx_reserva_pasajero_reserva_id
    ON reserva_pasajero(reserva_id);

CREATE INDEX idx_reserva_segmento_reserva_id
    ON reserva_segmento(reserva_id);

CREATE INDEX idx_boleto_reserva_id
    ON boleto(reserva_id);

CREATE INDEX idx_boleto_pasajero_id
    ON boleto(pasajero_id);

CREATE INDEX idx_boleto_vuelo_operado_id
    ON boleto(vuelo_operado_id);

CREATE INDEX idx_boleto_segmento_boleto_id
    ON boleto_segmento(boleto_id);

CREATE INDEX idx_boleto_asiento_boleto_segmento_id
    ON boleto_asiento(boleto_segmento_id);

CREATE INDEX idx_boleto_asiento_asiento_vuelo_id
    ON boleto_asiento(asiento_vuelo_id);

CREATE INDEX idx_equipaje_boleto_id
    ON equipaje(boleto_id);

CREATE INDEX idx_equipaje_pasajero_id
    ON equipaje(pasajero_id);