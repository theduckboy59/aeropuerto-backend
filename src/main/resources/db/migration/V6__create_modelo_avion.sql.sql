CREATE TABLE modelo_avion (
                              id SERIAL PRIMARY KEY,

                              fabricante VARCHAR(100) NOT NULL,

                              codigo_modelo VARCHAR(50) NOT NULL,

                              nombre VARCHAR(100) NOT NULL,

                              niveles INT NOT NULL,

                              pasillos INT NOT NULL,

                              configuracion VARCHAR(10) NOT NULL,

                              total_columnas INT NOT NULL,

                              filas_min INT NOT NULL,

                              filas_max INT NOT NULL,

                              estado_id INT NOT NULL,

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uq_modelo_avion_fabricante_codigo
                                  UNIQUE (fabricante, codigo_modelo),

                              CONSTRAINT ck_modelo_avion_niveles
                                  CHECK (niveles IN (1, 2)),

                              CONSTRAINT ck_modelo_avion_pasillos
                                  CHECK (pasillos > 0),

                              CONSTRAINT ck_modelo_avion_total_columnas
                                  CHECK (total_columnas > 0),

                              CONSTRAINT ck_modelo_avion_filas_min
                                  CHECK (filas_min > 0),

                              CONSTRAINT ck_modelo_avion_filas_max
                                  CHECK (filas_max >= filas_min),

                              CONSTRAINT ck_modelo_avion_configuracion
                                  CHECK (configuracion ~ '^[0-9]+(-[0-9]+)*$'),

    CONSTRAINT fk_modelo_avion_estado
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
        'ALTER TABLE modelo_avion ALTER COLUMN estado_id SET DEFAULT %s',
        v_activo_id
        );
END $$;