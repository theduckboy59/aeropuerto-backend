CREATE TABLE tipo_documento (
                                id SERIAL PRIMARY KEY,
                                nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE nacionalidad (
                              id SERIAL PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE codigo_area (
                             id SERIAL PRIMARY KEY,
                             codigo VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE pasajero (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER NULL,
                          tipo_documento_id INTEGER NOT NULL,
                          numero_documento VARCHAR(50) NOT NULL UNIQUE,
                          nombre_completo VARCHAR(150) NOT NULL,
                          fecha_nacimiento DATE NOT NULL,
                          nacionalidad_id INTEGER NOT NULL,
                          codigo_area_id INTEGER NOT NULL,
                          telefono VARCHAR(20) NOT NULL,
                          telefono_emergencia VARCHAR(20),
                          direccion VARCHAR(255),
                          estado_id INTEGER NOT NULL DEFAULT 1,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_pasajero_user FOREIGN KEY (user_id) REFERENCES users(id),
                          CONSTRAINT fk_pasajero_tipo_documento FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id),
                          CONSTRAINT fk_pasajero_nacionalidad FOREIGN KEY (nacionalidad_id) REFERENCES nacionalidad(id),
                          CONSTRAINT fk_pasajero_codigo_area FOREIGN KEY (codigo_area_id) REFERENCES codigo_area(id),
                          CONSTRAINT fk_pasajero_estado FOREIGN KEY (estado_id) REFERENCES status_catalog(id)
);

INSERT INTO tipo_documento (nombre) VALUES ('DPI');
INSERT INTO tipo_documento (nombre) VALUES ('PASAPORTE');
INSERT INTO tipo_documento (nombre) VALUES ('LICENCIA');

INSERT INTO nacionalidad (nombre) VALUES ('GUATEMALA');
INSERT INTO nacionalidad (nombre) VALUES ('EL SALVADOR');
INSERT INTO nacionalidad (nombre) VALUES ('MEXICO');

INSERT INTO codigo_area (codigo) VALUES ('+502');
INSERT INTO codigo_area (codigo) VALUES ('+503');
INSERT INTO codigo_area (codigo) VALUES ('+52');

CREATE INDEX idx_pasajero_user_id ON pasajero(user_id);
CREATE INDEX idx_pasajero_tipo_doc ON pasajero(tipo_documento_id);
CREATE INDEX idx_pasajero_nacionalidad ON pasajero(nacionalidad_id);
CREATE INDEX idx_pasajero_codigo_area ON pasajero(codigo_area_id);