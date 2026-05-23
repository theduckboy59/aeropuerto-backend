CREATE TABLE estado_reserva (
                                id SERIAL PRIMARY KEY,
                                nombre VARCHAR(100) NULL,
                                estado_id INT NULL DEFAULT 1,
                                created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE estado_boleto (
                               id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NULL,
                               estado_id INT NULL DEFAULT 1,
                               created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tipo_equipaje (
                               id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NULL,
                               estado_id INT NULL DEFAULT 1,
                               created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE estado_equipaje (
                                 id SERIAL PRIMARY KEY,
                                 nombre VARCHAR(100) NULL,
                                 estado_id INT NULL DEFAULT 1,
                                 created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reserva (
                         id SERIAL PRIMARY KEY,

                         codigo_reserva VARCHAR(100) NULL,

                         user_id INT NULL,
                         pasajero_id INT NULL,
                         vuelo_operado_id INT NULL,

                         estado_reserva_id INT NULL,

                         fecha_reserva TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                         subtotal NUMERIC(12,2) NULL,
                         recargo_total NUMERIC(12,2) NULL,
                         total NUMERIC(12,2) NULL,

                         observaciones VARCHAR(500) NULL,

                         estado_id INT NULL DEFAULT 1,
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
                                 REFERENCES estado_reserva(id)
);


CREATE TABLE reserva_pasajero (
                                  id SERIAL PRIMARY KEY,

                                  reserva_id INT NULL,
                                  pasajero_id INT NULL,

                                  estado_id INT NULL DEFAULT 1,
                                  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_reserva_pasajero_reserva
                                      FOREIGN KEY (reserva_id)
                                          REFERENCES reserva(id),

                                  CONSTRAINT fk_reserva_pasajero_pasajero
                                      FOREIGN KEY (pasajero_id)
                                          REFERENCES pasajero(id)
);

CREATE TABLE reserva_segmento (
                                  id SERIAL PRIMARY KEY,

                                  reserva_id INT NULL,
                                  segmento_operado_id INT NULL,

                                  orden_segmento INT NULL,

                                  estado_id INT NULL DEFAULT 1,
                                  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_reserva_segmento_reserva
                                      FOREIGN KEY (reserva_id)
                                          REFERENCES reserva(id),

                                  CONSTRAINT fk_reserva_segmento_segmento_operado
                                      FOREIGN KEY (segmento_operado_id)
                                          REFERENCES segmento_operado(id)
);


CREATE TABLE boleto (
                        id SERIAL PRIMARY KEY,

                        codigo_boleto VARCHAR(100) NULL,
                        codigo_pase_abordar VARCHAR(100) NULL,

                        reserva_id INT NULL,
                        pasajero_id INT NULL,
                        vuelo_operado_id INT NULL,

                        estado_boleto_id INT NULL,

                        fecha_emision TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                        precio_base NUMERIC(12,2) NULL,
                        recargo_equipaje NUMERIC(12,2) NULL,
                        total NUMERIC(12,2) NULL,

                        estado_id INT NULL DEFAULT 1,
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
                                REFERENCES estado_boleto(id)
);


CREATE TABLE boleto_segmento (
                                 id SERIAL PRIMARY KEY,

                                 boleto_id INT NULL,
                                 segmento_operado_id INT NULL,

                                 orden_segmento INT NULL,
                                 estado_boleto_id INT NULL,

                                 estado_id INT NULL DEFAULT 1,
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
                                         REFERENCES estado_boleto(id)
);


CREATE TABLE boleto_asiento (
                                id SERIAL PRIMARY KEY,

                                boleto_segmento_id INT NULL,
                                asiento_vuelo_id INT NULL,
                                clase_vuelo_id INT NULL,

                                estado_id INT NULL DEFAULT 1,
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
                                        REFERENCES clase_vuelo(id)
);


CREATE TABLE equipaje (
                          id SERIAL PRIMARY KEY,

                          boleto_id INT NULL,
                          pasajero_id INT NULL,
                          segmento_operado_id INT NULL,

                          tipo_equipaje_id INT NULL,
                          estado_equipaje_id INT NULL,

                          numero_maleta INT NULL,
                          descripcion VARCHAR(255) NULL,

                          peso NUMERIC(10,2) NULL,
                          monto_recargo NUMERIC(12,2) NULL,

                          estado_id INT NULL DEFAULT 1,
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
                                  REFERENCES estado_equipaje(id)
);


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


INSERT INTO estado_reserva (id, nombre, estado_id)
VALUES
    (1, 'CREADA', 1),
    (2, 'CONFIRMADA', 1),
    (3, 'CANCELADA', 1);

SELECT setval(
               pg_get_serial_sequence('estado_reserva', 'id'),
               (SELECT MAX(id) FROM estado_reserva)
       );


INSERT INTO estado_boleto (id, nombre, estado_id)
VALUES
    (1, 'PENDIENTE_ABORDAR', 1),
    (2, 'ABORDADO', 1),
    (3, 'CANCELADO', 1);

SELECT setval(
               pg_get_serial_sequence('estado_boleto', 'id'),
               (SELECT MAX(id) FROM estado_boleto)
       );


INSERT INTO tipo_equipaje (id, nombre, estado_id)
VALUES
    (1, 'MALETA', 1),
    (2, 'MANO', 1);

SELECT setval(
               pg_get_serial_sequence('tipo_equipaje', 'id'),
               (SELECT MAX(id) FROM tipo_equipaje)
       );


INSERT INTO estado_equipaje (id, nombre, estado_id)
VALUES
    (1, 'REGISTRADO', 1),
    (2, 'ABORDADO', 1),
    (3, 'CANCELADO', 1);

SELECT setval(
               pg_get_serial_sequence('estado_equipaje', 'id'),
               (SELECT MAX(id) FROM estado_equipaje)
       );