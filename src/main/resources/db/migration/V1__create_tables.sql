CREATE TABLE status_catalog (
                                id SERIAL PRIMARY KEY,
                                name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO status_catalog (name)
VALUES
    ('ACTIVO'),
    ('INACTIVO');

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       estado_id INTEGER NOT NULL,
                       rol_id INTEGER NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_users_estado
                           FOREIGN KEY (estado_id) REFERENCES status_catalog(id)
);

DO $$
DECLARE
v_activo_id INTEGER;
BEGIN
SELECT id
INTO v_activo_id
FROM status_catalog
WHERE UPPER(name) = 'ACTIVO';

EXECUTE format(
        'ALTER TABLE users ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;