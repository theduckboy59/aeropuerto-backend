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


INSERT INTO modelo_avion
(fabricante, codigo_modelo, nombre, niveles, pasillos, configuracion, total_columnas, filas_min, filas_max, estado_id)
VALUES

-- BOEING
('Boeing','737-700','Boeing 737-700',1,1,'3-3',6,20,30,1),
('Boeing','737-800','Boeing 737-800',1,1,'3-3',6,20,35,1),
('Boeing','737-900','Boeing 737-900',1,1,'3-3',6,25,40,1),
('Boeing','737-MAX8','Boeing 737 MAX 8',1,1,'3-3',6,25,40,1),
('Boeing','747-400','Boeing 747-400',2,2,'3-4-3',10,40,70,1),
('Boeing','747-8','Boeing 747-8',2,2,'3-4-3',10,45,76,1),
('Boeing','757-200','Boeing 757-200',1,1,'3-3',6,25,40,1),
('Boeing','767-300','Boeing 767-300',1,2,'2-3-2',7,25,45,1),
('Boeing','777-200','Boeing 777-200',1,2,'3-3-3',9,35,60,1),
('Boeing','777-300','Boeing 777-300',1,2,'3-4-3',10,40,65,1),
('Boeing','787-8','Boeing 787-8 Dreamliner',1,2,'3-3-3',9,30,50,1),
('Boeing','787-9','Boeing 787-9 Dreamliner',1,2,'3-3-3',9,35,60,1),

-- AIRBUS
('Airbus','A220-100','Airbus A220-100',1,1,'2-3',5,18,28,1),
('Airbus','A220-300','Airbus A220-300',1,1,'2-3',5,20,35,1),
('Airbus','A318','Airbus A318',1,1,'3-3',6,18,28,1),
('Airbus','A319','Airbus A319',1,1,'3-3',6,20,34,1),
('Airbus','A320','Airbus A320',1,1,'3-3',6,24,38,1),
('Airbus','A321','Airbus A321',1,1,'3-3',6,30,50,1),
('Airbus','A330-200','Airbus A330-200',1,2,'2-4-2',8,30,55,1),
('Airbus','A330-300','Airbus A330-300',1,2,'2-4-2',8,35,60,1),
('Airbus','A340-300','Airbus A340-300',1,2,'2-4-2',8,35,65,1),
('Airbus','A350-900','Airbus A350-900',1,2,'3-3-3',9,35,60,1),
('Airbus','A350-1000','Airbus A350-1000',1,2,'3-4-3',10,40,70,1),
('Airbus','A380-800','Airbus A380-800',2,2,'3-4-3',10,50,90,1),

-- EMBRAER
('Embraer','E170','Embraer E170',1,1,'2-2',4,15,25,1),
('Embraer','E175','Embraer E175',1,1,'2-2',4,18,30,1),
('Embraer','E190','Embraer E190',1,1,'2-2',4,20,35,1),
('Embraer','E195','Embraer E195',1,1,'2-2',4,22,38,1),
('Embraer','E195-E2','Embraer E195-E2',1,1,'2-2',4,25,40,1),

-- BOMBARDIER
('Bombardier','CRJ-200','Bombardier CRJ-200',1,1,'2-2',4,12,20,1),
('Bombardier','CRJ-700','Bombardier CRJ-700',1,1,'2-2',4,18,28,1),
('Bombardier','CRJ-900','Bombardier CRJ-900',1,1,'2-2',4,20,35,1),
('Bombardier','Q400','Bombardier Q400',1,1,'2-2',4,18,25,1),

-- ATR
('ATR','ATR-42','ATR 42',1,1,'2-2',4,10,18,1),
('ATR','ATR-72','ATR 72',1,1,'2-2',4,15,22,1),

-- CESSNA
('Cessna','208-Caravan','Cessna 208 Caravan',1,1,'1-1',2,5,10,1),
('Cessna','Citation-X','Cessna Citation X',1,1,'1-2',3,5,12,1),

-- GULFSTREAM
('Gulfstream','G550','Gulfstream G550',1,1,'1-1',2,6,15,1),
('Gulfstream','G650','Gulfstream G650',1,1,'1-1',2,8,18,1),

-- DASSAULT
('Dassault','Falcon-7X','Dassault Falcon 7X',1,1,'1-1',2,6,14,1),
('Dassault','Falcon-8X','Dassault Falcon 8X',1,1,'1-1',2,8,16,1),

-- BEECHCRAFT
('Beechcraft','1900D','Beechcraft 1900D',1,1,'1-1',2,6,10,1),
('Beechcraft','King-Air-350','Beechcraft King Air 350',1,1,'1-2',3,5,12,1),

-- LOCKHEED
('Lockheed','L1011','Lockheed L-1011 TriStar',1,2,'2-5-2',9,35,60,1),

-- MCDONNELL DOUGLAS
('McDonnell Douglas','MD-80','McDonnell Douglas MD-80',1,1,'2-3',5,20,35,1),
('McDonnell Douglas','MD-11','McDonnell Douglas MD-11',1,2,'2-5-2',9,35,60,1),

-- COMAC
('COMAC','C919','COMAC C919',1,1,'3-3',6,25,40,1),

-- IRKUT
('Irkut','MC-21','Irkut MC-21',1,1,'3-3',6,25,40,1),

-- SUKHOI
('Sukhoi','Superjet-100','Sukhoi Superjet 100',1,1,'2-3',5,18,30,1),

-- XIAN
('Xian','MA60','Xian MA60',1,1,'2-2',4,15,22,1),

-- ANTONOV
('Antonov','An-148','Antonov An-148',1,1,'2-3',5,18,30,1);