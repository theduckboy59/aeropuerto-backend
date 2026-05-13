CREATE TABLE status_catalog (
                                id SERIAL PRIMARY KEY,
                                name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       estado_id INTEGER NOT NULL DEFAULT 1,
                       rol_id INTEGER NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,

                       CONSTRAINT fk_users_estado FOREIGN KEY (estado_id) REFERENCES status_catalog(id)
);

INSERT INTO status_catalog (name) VALUES ('ACTIVO');
INSERT INTO status_catalog (name) VALUES ('INACTIVO');