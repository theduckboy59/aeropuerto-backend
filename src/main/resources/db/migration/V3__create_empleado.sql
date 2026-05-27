CREATE TABLE aerolinea (
                           id SERIAL PRIMARY KEY,
                           nombre VARCHAR(150) NOT NULL UNIQUE,
                           codigo_iata VARCHAR(10),
                           codigo_icao VARCHAR(10),
                           pais VARCHAR(100),
                           estado_id INT NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_aerolinea_estado
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
        'ALTER TABLE aerolinea ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;

INSERT INTO aerolinea (
    nombre,
    codigo_iata,
    codigo_icao,
    pais
)
VALUES
    ('Avianca', 'AV', 'AVA', 'Colombia'),
    ('Copa Airlines', 'CM', 'CMP', 'Panamá');

CREATE TABLE tipo_empleado (
                               id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE turno (
                       id SERIAL PRIMARY KEY,
                       nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE nivel_acceso (
                              id SERIAL PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE rol (
                     id SERIAL PRIMARY KEY,
                     nombre VARCHAR(100) NOT NULL UNIQUE
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_rol
        FOREIGN KEY (rol_id) REFERENCES rol(id);

CREATE INDEX idx_users_rol_id ON users(rol_id);

CREATE TABLE area (
                      id SERIAL PRIMARY KEY,
                      nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE licencia (
                          id SERIAL PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE empleado (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER NOT NULL,
                          tipo_empleado_id INTEGER NOT NULL,
                          aerolinea_id INTEGER NOT NULL,
                          codigo_empleado VARCHAR(100) NOT NULL UNIQUE,
                          nombre_completo VARCHAR(150) NOT NULL,
                          fecha_ingreso DATE NOT NULL,
                          fecha_salida DATE,
                          turno_id INTEGER NOT NULL,
                          nivel_acceso_id INTEGER NOT NULL,
                          rol_id INTEGER NOT NULL,
                          area_id INTEGER NOT NULL,
                          licencia_id INTEGER,
                          fecha_vencimiento_licencia DATE,
                          estado_id INTEGER NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CHECK (fecha_salida IS NULL OR fecha_salida >= fecha_ingreso),

                          CONSTRAINT fk_empleado_user FOREIGN KEY (user_id) REFERENCES users(id),
                          CONSTRAINT fk_empleado_tipo FOREIGN KEY (tipo_empleado_id) REFERENCES tipo_empleado(id),
                          CONSTRAINT fk_empleado_aerolinea FOREIGN KEY (aerolinea_id) REFERENCES aerolinea(id),
                          CONSTRAINT fk_empleado_turno FOREIGN KEY (turno_id) REFERENCES turno(id),
                          CONSTRAINT fk_empleado_nivel FOREIGN KEY (nivel_acceso_id) REFERENCES nivel_acceso(id),
                          CONSTRAINT fk_empleado_rol FOREIGN KEY (rol_id) REFERENCES rol(id),
                          CONSTRAINT fk_empleado_area FOREIGN KEY (area_id) REFERENCES area(id),
                          CONSTRAINT fk_empleado_licencia FOREIGN KEY (licencia_id) REFERENCES licencia(id),
                          CONSTRAINT fk_empleado_estado FOREIGN KEY (estado_id) REFERENCES status_catalog(id)
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
        'ALTER TABLE empleado ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;

INSERT INTO tipo_empleado (nombre) VALUES ('PILOTO');
INSERT INTO tipo_empleado (nombre) VALUES ('COPILOTO');
INSERT INTO tipo_empleado (nombre) VALUES ('CABINA');
INSERT INTO tipo_empleado (nombre) VALUES ('INGENIERO_VUELO');
INSERT INTO tipo_empleado (nombre) VALUES ('ADMINISTRATIVO');

INSERT INTO turno (nombre) VALUES ('MATUTINO');
INSERT INTO turno (nombre) VALUES ('VESPERTINO');
INSERT INTO turno (nombre) VALUES ('NOCTURNO');

INSERT INTO nivel_acceso (nombre) VALUES ('ALTO');
INSERT INTO nivel_acceso (nombre) VALUES ('MEDIO');
INSERT INTO nivel_acceso (nombre) VALUES ('BASICO');

INSERT INTO rol (nombre) VALUES ('CLIENTE');
INSERT INTO rol (nombre) VALUES ('ADMIN_AEROLINEA');
INSERT INTO rol (nombre) VALUES ('ADMIN_ABORDAJE');
INSERT INTO rol (nombre) VALUES ('EMPLEADO_AEROLINEA');
INSERT INTO rol (nombre) VALUES ('ADMIN_SISTEMA');

INSERT INTO area (nombre) VALUES ('OPERACIONES');
INSERT INTO area (nombre) VALUES ('CABINA');
INSERT INTO area (nombre) VALUES ('TI');
INSERT INTO area (nombre) VALUES ('ABORDAJE');
INSERT INTO area (nombre) VALUES ('ADMINISTRACION');

INSERT INTO licencia (nombre) VALUES ('COMERCIAL');
INSERT INTO licencia (nombre) VALUES ('PRIVADA');
INSERT INTO licencia (nombre) VALUES ('INSTRUCTOR');

CREATE INDEX idx_empleado_user_id ON empleado(user_id);
CREATE INDEX idx_empleado_tipo ON empleado(tipo_empleado_id);
CREATE INDEX idx_empleado_aerolinea ON empleado(aerolinea_id);
CREATE INDEX idx_empleado_turno ON empleado(turno_id);
CREATE INDEX idx_empleado_nivel ON empleado(nivel_acceso_id);
CREATE INDEX idx_empleado_rol ON empleado(rol_id);
CREATE INDEX idx_empleado_area ON empleado(area_id);
CREATE INDEX idx_empleado_licencia ON empleado(licencia_id);
CREATE INDEX idx_empleado_estado ON empleado(estado_id);