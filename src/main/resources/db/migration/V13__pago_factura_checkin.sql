CREATE TABLE metodo_pago (
                             id SERIAL PRIMARY KEY,
                             nombre VARCHAR(100) NULL,
                             estado_id INTEGER NULL DEFAULT 1,
                             created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_metodo_pago_estado
                                 FOREIGN KEY (estado_id)
                                     REFERENCES status_catalog(id)
);

CREATE TABLE estado_pago (
                             id SERIAL PRIMARY KEY,
                             nombre VARCHAR(100) NULL,
                             estado_id INTEGER NULL DEFAULT 1,
                             created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_estado_pago_estado
                                 FOREIGN KEY (estado_id)
                                     REFERENCES status_catalog(id)
);

CREATE TABLE pago (
                      id SERIAL PRIMARY KEY,
                      reserva_id INTEGER NULL,
                      metodo_pago_id INTEGER NULL,
                      monto DECIMAL(10,2) NULL,
                      recargo_equipaje DECIMAL(10,2) NULL DEFAULT 0,
                      estado_pago_id INTEGER NULL,
                      fecha_pago TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                      fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT fk_pago_reserva
                          FOREIGN KEY (reserva_id)
                              REFERENCES reserva(id),

                      CONSTRAINT fk_pago_metodo_pago
                          FOREIGN KEY (metodo_pago_id)
                              REFERENCES metodo_pago(id),

                      CONSTRAINT fk_pago_estado_pago
                          FOREIGN KEY (estado_pago_id)
                              REFERENCES estado_pago(id)
);

CREATE TABLE factura (
                         id SERIAL PRIMARY KEY,
                         pago_id INTEGER NULL,
                         nit VARCHAR(20) NULL,
                         nombre_cliente VARCHAR(150) NULL,
                         serie VARCHAR(20) NULL,
                         numero VARCHAR(30) NULL,
                         uuid_fel VARCHAR(100) NULL,
                         estado_fel VARCHAR(50) NULL,
                         fecha_factura TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                         fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                         created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_factura_pago
                             FOREIGN KEY (pago_id)
                                 REFERENCES pago(id)
);

CREATE TABLE estado_checkin (
                                id SERIAL PRIMARY KEY,
                                nombre VARCHAR(100) NULL,
                                estado_id INTEGER NULL DEFAULT 1,
                                created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_estado_checkin_estado
                                    FOREIGN KEY (estado_id)
                                        REFERENCES status_catalog(id)
);

CREATE TABLE checkin (
                         id SERIAL PRIMARY KEY,
                         boleto_segmento_id INTEGER NULL,
                         estado_checkin_id INTEGER NULL,
                         tipo_checkin VARCHAR(50) NULL,
                         fecha_checkin TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                         fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                         empleado_id INTEGER NULL,
                         created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_checkin_boleto_segmento
                             FOREIGN KEY (boleto_segmento_id)
                                 REFERENCES boleto_segmento(id),

                         CONSTRAINT fk_checkin_estado_checkin
                             FOREIGN KEY (estado_checkin_id)
                                 REFERENCES estado_checkin(id),

                         CONSTRAINT fk_checkin_empleado
                             FOREIGN KEY (empleado_id)
                                 REFERENCES empleado(id)
);

CREATE TABLE historial_boleto (
                                  id SERIAL PRIMARY KEY,
                                  boleto_id INTEGER NULL,
                                  tipo_accion VARCHAR(50) NULL,
                                  descripcion VARCHAR(255) NULL,
                                  fecha TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_historial_boleto_boleto
                                      FOREIGN KEY (boleto_id)
                                          REFERENCES boleto(id)
);

CREATE INDEX idx_pago_reserva_id ON pago(reserva_id);
CREATE INDEX idx_pago_estado_pago_id ON pago(estado_pago_id);
CREATE INDEX idx_factura_pago_id ON factura(pago_id);
CREATE INDEX idx_checkin_boleto_segmento_id ON checkin(boleto_segmento_id);
CREATE INDEX idx_checkin_estado_checkin_id ON checkin(estado_checkin_id);
CREATE INDEX idx_historial_boleto_boleto_id ON historial_boleto(boleto_id);

INSERT INTO metodo_pago (id, nombre, estado_id)
VALUES
    (1, 'EFECTIVO', 1),
    (2, 'TARJETA_CREDITO', 1),
    (3, 'TARJETA_DEBITO', 1),
    (4, 'TRANSFERENCIA', 1);

SELECT setval(
               pg_get_serial_sequence('metodo_pago', 'id'),
               (SELECT MAX(id) FROM metodo_pago)
       );

INSERT INTO estado_pago (id, nombre, estado_id)
VALUES
    (1, 'PENDIENTE', 1),
    (2, 'PAGADO', 1),
    (3, 'RECHAZADO', 1),
    (4, 'ANULADO', 1);

SELECT setval(
               pg_get_serial_sequence('estado_pago', 'id'),
               (SELECT MAX(id) FROM estado_pago)
       );

INSERT INTO estado_checkin (id, nombre, estado_id)
VALUES
    (1, 'PENDIENTE', 1),
    (2, 'REALIZADO', 1),
    (3, 'CANCELADO', 1);

SELECT setval(
               pg_get_serial_sequence('estado_checkin', 'id'),
               (SELECT MAX(id) FROM estado_checkin)
       );