CREATE TABLE aerolinea (
                           id SERIAL PRIMARY KEY,
                           nombre VARCHAR(150) NOT NULL UNIQUE,
                           estado VARCHAR(50)
);

INSERT INTO aerolinea (nombre, estado) VALUES ('AEROLINEA DEMO', 'ACTIVA');

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
                          estado_id INTEGER NOT NULL DEFAULT 1,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

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

INSERT INTO tipo_empleado (nombre) VALUES ('PILOTO');
INSERT INTO tipo_empleado (nombre) VALUES ('COPILOTO');
INSERT INTO tipo_empleado (nombre) VALUES ('CABINA');

INSERT INTO turno (nombre) VALUES ('MATUTINO');
INSERT INTO turno (nombre) VALUES ('VESPERTINO');
INSERT INTO turno (nombre) VALUES ('NOCTURNO');

INSERT INTO nivel_acceso (nombre) VALUES ('ADMIN');
INSERT INTO nivel_acceso (nombre) VALUES ('SUPERVISOR');
INSERT INTO nivel_acceso (nombre) VALUES ('OPERATIVO');

INSERT INTO rol (nombre) VALUES ('ADMIN');
INSERT INTO rol (nombre) VALUES ('CHECKIN');
INSERT INTO rol (nombre) VALUES ('OPERADOR');

INSERT INTO area (nombre) VALUES ('OPERACIONES');
INSERT INTO area (nombre) VALUES ('CABINA');
INSERT INTO area (nombre) VALUES ('TI');

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
