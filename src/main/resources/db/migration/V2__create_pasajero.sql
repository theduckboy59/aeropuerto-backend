CREATE TABLE pasajero (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER NULL,
                          pasaporte VARCHAR(20) NOT NULL UNIQUE,
                          nombre_completo VARCHAR(150) NOT NULL,
                          fecha_nacimiento DATE NOT NULL,
                          nacionalidad VARCHAR(100) NOT NULL,
                          codigo_area VARCHAR(10) NULL,
                          telefono VARCHAR(20) NULL,
                          telefono_emergencia VARCHAR(20) NOT NULL,
                          direccion VARCHAR(255),
                          estado_id INTEGER NOT NULL DEFAULT 1,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_pasajero_user FOREIGN KEY (user_id) REFERENCES users(id),
                          CONSTRAINT fk_pasajero_estado FOREIGN KEY (estado_id) REFERENCES status_catalog(id)
);

CREATE INDEX idx_pasajero_user_id ON pasajero(user_id);
CREATE INDEX idx_pasajero_pasaporte ON pasajero(pasaporte);