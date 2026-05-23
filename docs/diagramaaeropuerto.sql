CREATE TABLE Usuario (
    id_usuario SERIAL PRIMARY KEY,
    correo VARCHAR(150) UNIQUE,
    contrasena VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

CREATE TABLE TipoEmpleado (
    id_tipo_empleado INT PRIMARY KEY,
    nombre_tipo_empleado VARCHAR(100) UNIQUE
);

CREATE TABLE Nacionalidad (
    id_nacionalidad INT PRIMARY KEY,
    nombre_nacionalidad VARCHAR(100) UNIQUE
);

CREATE TABLE MetodoPago (
    id_metodo_pago INT PRIMARY KEY,
    nombre_metodo_pago VARCHAR(50) UNIQUE
);

CREATE TABLE TipoEquipaje (
    id_tipo_equipaje INT PRIMARY KEY,
    nombre_tipo_equipaje VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoReserva (
    id_estado_reserva INT PRIMARY KEY,
    nombre_estado_reserva VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoAvion (
    id_estado_avion INT PRIMARY KEY,
    nombre_estado_avion VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoVuelo (
    id_estado_vuelo INT PRIMARY KEY,
    nombre_estado_vuelo VARCHAR(100) UNIQUE
);

CREATE TABLE ClaseVuelo (
    id_clase_vuelo INT PRIMARY KEY,
    nombre_clase_vuelo VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoAsiento (
    id_estado_asiento INT PRIMARY KEY,
    nombre_estado_asiento VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoEquipaje (
    id_estado_equipaje INT PRIMARY KEY,
    nombre_estado_equipaje VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoBoleto (
    id_estado_boleto INT PRIMARY KEY,
    nombre_estado_boleto VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoAbordajeVuelo (
    id_estado_abordaje_vuelo INT PRIMARY KEY,
    nombre_estado_abordaje_vuelo VARCHAR(100) UNIQUE
);

CREATE TABLE TipoSegmentoVuelo (
    id_tipo_segmento_vuelo INT PRIMARY KEY,
    nombre_tipo_segmento_vuelo VARCHAR(100) UNIQUE
);

CREATE TABLE EstadoTripulacion (
    id_estado_tripulacion INT PRIMARY KEY,
    nombre_estado_tripulacion VARCHAR(50) UNIQUE
);

CREATE TABLE EstadoPago (
    id_estado_pago INT PRIMARY KEY,
    nombre_estado_pago VARCHAR(50) UNIQUE
);

CREATE TABLE Aerolinea (
    id_aerolinea INT PRIMARY KEY,
    nombre_aerolinea VARCHAR(150) UNIQUE,
    estado_aerolinea VARCHAR(50)
);

CREATE TABLE Aeropuerto (
    id_aeropuerto INT PRIMARY KEY,
    nombre_aeropuerto VARCHAR(150),
    ciudad VARCHAR(100),
    pais VARCHAR(100),
    UNIQUE(nombre_aeropuerto, ciudad, pais)
);

CREATE TABLE AerolineaDestino (
    id_aerolinea_destino SERIAL PRIMARY KEY,
    id_aerolinea INT NOT NULL,
    id_aeropuerto INT NOT NULL,
    fecha_autorizacion DATE,
    UNIQUE (id_aerolinea, id_aeropuerto),
    FOREIGN KEY (id_aerolinea) REFERENCES Aerolinea(id_aerolinea),
    FOREIGN KEY (id_aeropuerto) REFERENCES Aeropuerto(id_aeropuerto)
);

CREATE TABLE Pasajero (
    id_pasajero INT PRIMARY KEY,
    id_usuario INT UNIQUE,
    numero_pasaporte VARCHAR(50) UNIQUE,
    nombre_completo VARCHAR(150),
    fecha_nacimiento DATE,
    id_nacionalidad INT,
    codigo_area VARCHAR(10),
    telefono VARCHAR(20),
    telefono_emergencia VARCHAR(20),
    direccion VARCHAR(255),
    estado_registro BOOLEAN,
    fecha_actualizacion TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_nacionalidad) REFERENCES Nacionalidad(id_nacionalidad)
);

CREATE TABLE Empleado (
    id_empleado SERIAL PRIMARY KEY,
    id_usuario INT UNIQUE,
    id_tipo_empleado INT,
    id_aerolinea INT,
    nombre_completo VARCHAR(150),
    fecha_ingreso DATE NOT NULL,
    fecha_salida DATE,
    turno VARCHAR(50),
    estado_registro BOOLEAN,
    fecha_actualizacion TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_tipo_empleado) REFERENCES TipoEmpleado(id_tipo_empleado),
    FOREIGN KEY (id_aerolinea) REFERENCES Aerolinea(id_aerolinea)
);

CREATE TABLE DisponibilidadEmpleado (
    id_disponibilidad INT PRIMARY KEY,
    id_empleado INT,
    fecha DATE,
    hora_inicio TIME,
    hora_fin TIME,
    disponible BOOLEAN,
    FOREIGN KEY (id_empleado) REFERENCES Empleado(id_empleado)
);

CREATE TABLE PuertaEmbarque (
    id_puerta_embarque INT PRIMARY KEY,
    id_aeropuerto INT,
    codigo_puerta VARCHAR(50),
    estado_puerta VARCHAR(50),
    UNIQUE(id_aeropuerto, codigo_puerta),
    FOREIGN KEY (id_aeropuerto) REFERENCES Aeropuerto(id_aeropuerto)
);

CREATE TABLE ModeloAvion (
    id_modelo SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    niveles INT NOT NULL,
    tipo_niveles VARCHAR(20),
    pasillos INT NOT NULL,
    configuracion VARCHAR(10) NOT NULL, 
    total_columnas INT NOT NULL,
    filas_min INT NOT NULL,
    filas_max INT NOT NULL,
    estado BOOLEAN DEFAULT TRUE,
    CHECK (filas_min <= filas_max),
    CHECK (configuracion ~ '^[0-9]+(-[0-9]+)*$')
);

CREATE TABLE Avion (
    id_avion SERIAL PRIMARY KEY,
    id_aerolinea INT NOT NULL,
    id_estado_avion INT NOT NULL,
    id_modelo INT NOT NULL,
    codigo_avion VARCHAR(50) UNIQUE NOT NULL,
    anio INT,
    capacidad_pasajeros INT,
    filas_configuradas INT NOT NULL,
    FOREIGN KEY (id_aerolinea) REFERENCES Aerolinea(id_aerolinea),
    FOREIGN KEY (id_estado_avion) REFERENCES EstadoAvion(id_estado_avion),
    FOREIGN KEY (id_modelo) REFERENCES ModeloAvion(id_modelo)
);


CREATE TABLE Tripulacion (
    id_tripulacion INT PRIMARY KEY,
    id_estado_tripulacion INT,
    fecha_creacion TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    FOREIGN KEY (id_estado_tripulacion) REFERENCES EstadoTripulacion(id_estado_tripulacion)
);

CREATE TABLE TripulacionDetalle (
    id_tripulacion_detalle SERIAL PRIMARY KEY,
    id_tripulacion INT NOT NULL,
    id_empleado INT NOT NULL,
    rol_tripulacion VARCHAR(100),
    UNIQUE (id_tripulacion, id_empleado),
    FOREIGN KEY (id_tripulacion) REFERENCES Tripulacion(id_tripulacion),
    FOREIGN KEY (id_empleado) REFERENCES Empleado(id_empleado)
);

CREATE TABLE Vuelo (
    id_vuelo INT PRIMARY KEY,
    id_aerolinea INT,
    codigo_vuelo VARCHAR(50) UNIQUE,
    FOREIGN KEY (id_aerolinea) REFERENCES Aerolinea(id_aerolinea)
);

CREATE TABLE VueloProgramado (
    id_vuelo_programado INT PRIMARY KEY,
    id_vuelo INT,
    id_aeropuerto_salida INT,
    id_aeropuerto_llegada INT,
    fecha_salida DATE,
    hora_salida TIME,
    fecha_llegada DATE,
    hora_llegada TIME,
    fecha_actualizacion TIMESTAMP,
    UNIQUE(id_vuelo, fecha_salida, hora_salida),
    CHECK (id_aeropuerto_salida <> id_aeropuerto_llegada),
    FOREIGN KEY (id_vuelo) REFERENCES Vuelo(id_vuelo),
    FOREIGN KEY (id_aeropuerto_salida) REFERENCES Aeropuerto(id_aeropuerto),
    FOREIGN KEY (id_aeropuerto_llegada) REFERENCES Aeropuerto(id_aeropuerto)
);

CREATE TABLE VueloOperado (
    id_vuelo_operado INT PRIMARY KEY,
    id_vuelo_programado INT NOT NULL,
    id_avion INT NOT NULL,
    id_tripulacion INT NOT NULL,
    id_puerta_embarque INT NOT NULL,
    id_estado_vuelo INT NOT NULL,
    fecha_salida_real DATE,
    hora_salida_real TIME,
    fecha_llegada_real DATE,
    hora_llegada_real TIME,
    fecha_actualizacion TIMESTAMP,
    FOREIGN KEY (id_vuelo_programado) REFERENCES VueloProgramado(id_vuelo_programado),
    FOREIGN KEY (id_avion) REFERENCES Avion(id_avion),
    FOREIGN KEY (id_tripulacion) REFERENCES Tripulacion(id_tripulacion),
    FOREIGN KEY (id_puerta_embarque) REFERENCES PuertaEmbarque(id_puerta_embarque),
    FOREIGN KEY (id_estado_vuelo) REFERENCES EstadoVuelo(id_estado_vuelo)
);

CREATE TABLE PrecioVuelo (
    id_precio INT PRIMARY KEY,
    id_vuelo_programado INT NOT NULL,
    id_clase_vuelo INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    CHECK (precio > 0),
    CHECK (fecha_vigencia_hasta IS NULL OR fecha_vigencia_hasta >= fecha_vigencia_desde),
    FOREIGN KEY (id_vuelo_programado) REFERENCES VueloProgramado(id_vuelo_programado),
    FOREIGN KEY (id_clase_vuelo) REFERENCES ClaseVuelo(id_clase_vuelo)
);

CREATE TABLE RecargoAsientoTipo (
    id_recargo_asiento_tipo SERIAL PRIMARY KEY,
    id_vuelo_programado INT NOT NULL,
    id_clase_vuelo INT NOT NULL,
    tipo_asiento VARCHAR(20) NOT NULL,
    recargo DECIMAL(10,2) NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CHECK (recargo >= 0),
    UNIQUE (id_vuelo_programado, id_clase_vuelo, tipo_asiento),
    FOREIGN KEY (id_vuelo_programado) REFERENCES VueloProgramado(id_vuelo_programado),
    FOREIGN KEY (id_clase_vuelo) REFERENCES ClaseVuelo(id_clase_vuelo)
);

CREATE TABLE SegmentoVuelo (
    id_segmento_vuelo INT PRIMARY KEY,
    id_vuelo_programado INT NOT NULL,
    id_aeropuerto_salida INT NOT NULL,
    id_aeropuerto_llegada INT NOT NULL,
    id_tipo_segmento_vuelo INT NOT NULL,
    orden_segmento INT NOT NULL,
    UNIQUE(id_vuelo_programado, orden_segmento),
    CHECK (id_aeropuerto_salida <> id_aeropuerto_llegada),
    FOREIGN KEY (id_vuelo_programado) REFERENCES VueloProgramado(id_vuelo_programado),
    FOREIGN KEY (id_aeropuerto_salida) REFERENCES Aeropuerto(id_aeropuerto),
    FOREIGN KEY (id_aeropuerto_llegada) REFERENCES Aeropuerto(id_aeropuerto),
    FOREIGN KEY (id_tipo_segmento_vuelo) REFERENCES TipoSegmentoVuelo(id_tipo_segmento_vuelo)
);


CREATE TABLE SegmentoOperado (
    id_segmento_operado INT PRIMARY KEY,
    id_vuelo_operado INT NOT NULL,
    id_segmento_vuelo INT NOT NULL,
    UNIQUE(id_segmento_operado, id_segmento_vuelo),
    UNIQUE(id_vuelo_operado, id_segmento_vuelo),
    FOREIGN KEY (id_vuelo_operado) REFERENCES VueloOperado(id_vuelo_operado),
    FOREIGN KEY (id_segmento_vuelo) REFERENCES SegmentoVuelo(id_segmento_vuelo)
);


CREATE TABLE Reserva (
    id_reserva INT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_estado_reserva INT,
    fecha_reserva TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_estado_reserva) REFERENCES EstadoReserva(id_estado_reserva)
);

CREATE TABLE ReservaPasajero (
    id_reserva_pasajero SERIAL PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_pasajero INT NOT NULL,
    UNIQUE (id_reserva, id_pasajero),
    FOREIGN KEY (id_reserva) REFERENCES Reserva(id_reserva),
    FOREIGN KEY (id_pasajero) REFERENCES Pasajero(id_pasajero)
);

CREATE TABLE ReservaSegmento (
    id_reserva_segmento SERIAL PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_segmento_vuelo INT NOT NULL,
    orden INT NOT NULL,
    UNIQUE (id_reserva, id_segmento_vuelo),
    UNIQUE (id_reserva, orden),
    FOREIGN KEY (id_reserva) REFERENCES Reserva(id_reserva),
    FOREIGN KEY (id_segmento_vuelo) REFERENCES SegmentoVuelo(id_segmento_vuelo)
);

CREATE TABLE Pago (
    id_pago INT PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_metodo_pago INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    recargo_equipaje DECIMAL(10,2),
    id_estado_pago INT NOT NULL,
    fecha_pago TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    FOREIGN KEY (id_reserva) REFERENCES Reserva(id_reserva),
    FOREIGN KEY (id_metodo_pago) REFERENCES MetodoPago(id_metodo_pago),
    FOREIGN KEY (id_estado_pago) REFERENCES EstadoPago(id_estado_pago)
);

CREATE TABLE Factura (
    id_factura INT PRIMARY KEY,
    id_pago INT NOT NULL UNIQUE,
    nit VARCHAR(20),
    nombre_cliente VARCHAR(150),
    direccion_cliente VARCHAR(255),
    serie VARCHAR(20),
    numero VARCHAR(30),
    uuid_fel VARCHAR(100) UNIQUE,
    estado_fel VARCHAR(30),
    fecha_certificacion TIMESTAMP,
    fecha_factura TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES Pago(id_pago)
);


CREATE TABLE Boleto (
    id_boleto INT PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_pasajero INT NOT NULL,
    id_clase_vuelo INT NOT NULL,
    id_estado_boleto INT NOT NULL,
    fecha_emision DATE,
    fecha_actualizacion TIMESTAMP,
    UNIQUE (id_boleto, id_reserva),
    FOREIGN KEY (id_reserva, id_pasajero) REFERENCES ReservaPasajero(id_reserva, id_pasajero), 
    FOREIGN KEY (id_clase_vuelo) REFERENCES ClaseVuelo(id_clase_vuelo),
    FOREIGN KEY (id_estado_boleto) REFERENCES EstadoBoleto(id_estado_boleto)
);

CREATE TABLE BoletoSegmento (
    id_boleto_segmento SERIAL PRIMARY KEY,
    id_boleto INT NOT NULL,
    id_reserva INT NOT NULL,
    id_segmento_vuelo INT NOT NULL,
    id_segmento_operado INT,
    precio_aplicado DECIMAL(10,2) NOT NULL,
    fecha_actualizacion TIMESTAMP,
    UNIQUE (id_boleto, id_segmento_vuelo),
    CHECK (precio_aplicado >= 0),
    FOREIGN KEY (id_boleto, id_reserva) REFERENCES Boleto(id_boleto, id_reserva),
    FOREIGN KEY (id_reserva, id_segmento_vuelo) REFERENCES ReservaSegmento(id_reserva, id_segmento_vuelo),
    FOREIGN KEY (id_segmento_vuelo) REFERENCES SegmentoVuelo(id_segmento_vuelo),
    FOREIGN KEY (id_segmento_operado, id_segmento_vuelo) REFERENCES SegmentoOperado(id_segmento_operado, id_segmento_vuelo)
);


CREATE TABLE EstadoCheckIn (
    id_estado_checkin INT PRIMARY KEY,
    nombre_estado_checkin VARCHAR(50) UNIQUE
);

CREATE TABLE CheckIn (
    id_checkin SERIAL PRIMARY KEY,
    id_boleto_segmento INT NOT NULL,
    id_estado_checkin INT NOT NULL,
    tipo_checkin VARCHAR(20) NOT NULL,
    fecha_checkin TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    id_empleado INT,
    UNIQUE(id_boleto_segmento),
    CHECK (tipo_checkin IN ('QR', 'MANUAL', 'WEB')),
    FOREIGN KEY (id_boleto_segmento) REFERENCES BoletoSegmento(id_boleto_segmento),
    FOREIGN KEY (id_estado_checkin) REFERENCES EstadoCheckIn(id_estado_checkin),
    FOREIGN KEY (id_empleado) REFERENCES Empleado(id_empleado)
);

CREATE TABLE HistorialBoleto (
    id_historial INT PRIMARY KEY,
    id_boleto INT,
    tipo_accion VARCHAR(50),
    descripcion VARCHAR(255),
    fecha TIMESTAMP,
    FOREIGN KEY (id_boleto) REFERENCES Boleto(id_boleto)
);

CREATE TABLE ConfigClaseFilasAvion (
    id_config_clase_filas SERIAL PRIMARY KEY,
    id_avion INT NOT NULL,
    id_clase_vuelo INT NOT NULL,
    fila_desde INT NOT NULL,
    fila_hasta INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_actualizacion TIMESTAMP,
    CHECK (fila_desde > 0),
    CHECK (fila_hasta >= fila_desde),
    FOREIGN KEY (id_avion) REFERENCES Avion(id_avion),
    FOREIGN KEY (id_clase_vuelo) REFERENCES ClaseVuelo(id_clase_vuelo)
);


CREATE TABLE AsientoUbi (
    id_asiento SERIAL PRIMARY KEY,
    id_avion INT NOT NULL,
    id_clase_vuelo INT NOT NULL,
    nivel INT DEFAULT 1 CHECK (nivel IN (1,2)),
    fila INT NOT NULL,
    columna VARCHAR(2) NOT NULL,
    numero_asiento VARCHAR(10) NOT NULL,
    bloque INT,
    lado VARCHAR(10),
    tipo_asiento VARCHAR(20),
    UNIQUE(id_avion, nivel, numero_asiento),
    FOREIGN KEY (id_avion) REFERENCES Avion(id_avion),
    FOREIGN KEY (id_clase_vuelo) REFERENCES ClaseVuelo(id_clase_vuelo)
);

CREATE TABLE AsientoVuelo (
    id_asiento_vuelo INT PRIMARY KEY,
    id_segmento_operado INT NOT NULL,
    id_asiento INT NOT NULL,
    id_estado_asiento INT NOT NULL,
    UNIQUE(id_asiento_vuelo, id_segmento_operado),
    UNIQUE(id_segmento_operado, id_asiento),
    FOREIGN KEY (id_segmento_operado) REFERENCES SegmentoOperado(id_segmento_operado),
    FOREIGN KEY (id_asiento) REFERENCES AsientoUbi(id_asiento),
    FOREIGN KEY (id_estado_asiento) REFERENCES EstadoAsiento(id_estado_asiento)
);

CREATE TABLE BoletoAsiento (
    id_boleto_asiento SERIAL PRIMARY KEY,
    id_boleto_segmento INT NOT NULL,
    id_asiento_vuelo INT NOT NULL UNIQUE,
    FOREIGN KEY (id_boleto_segmento) REFERENCES BoletoSegmento(id_boleto_segmento),
    FOREIGN KEY (id_asiento_vuelo) REFERENCES AsientoVuelo(id_asiento_vuelo)
);

CREATE TABLE Equipaje (
    id_equipaje INT PRIMARY KEY,
    id_boleto_segmento INT NOT NULL,
    id_tipo_equipaje INT NOT NULL,
    peso DECIMAL(10,2) NOT NULL,
    costo DECIMAL(10,2) NOT NULL DEFAULT 0,
    id_estado_equipaje INT NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CHECK (costo >= 0),
    FOREIGN KEY (id_boleto_segmento) REFERENCES BoletoSegmento(id_boleto_segmento),
    FOREIGN KEY (id_tipo_equipaje) REFERENCES TipoEquipaje(id_tipo_equipaje),
    FOREIGN KEY (id_estado_equipaje) REFERENCES EstadoEquipaje(id_estado_equipaje)
);

CREATE TABLE Abordaje (
    id_abordaje INT PRIMARY KEY,
    id_boleto_segmento INT NOT NULL,
    id_empleado INT,
    id_puerta_embarque INT,
    id_estado_abordaje_vuelo INT,
    tipo_abordaje VARCHAR(20) NOT NULL,
    fecha_abordaje DATE,
    hora_abordaje TIME,
    boleto_validado BOOLEAN,
    fecha_actualizacion TIMESTAMP,
    UNIQUE(id_boleto_segmento),
    CHECK (tipo_abordaje IN ('QR', 'MANUAL')),
    FOREIGN KEY (id_boleto_segmento) REFERENCES BoletoSegmento(id_boleto_segmento),
    FOREIGN KEY (id_empleado) REFERENCES Empleado(id_empleado),
    FOREIGN KEY (id_puerta_embarque) REFERENCES PuertaEmbarque(id_puerta_embarque),
    FOREIGN KEY (id_estado_abordaje_vuelo) REFERENCES EstadoAbordajeVuelo(id_estado_abordaje_vuelo)
);

CREATE TABLE BitacoraSistema (
    id_bitacora_sistema SERIAL PRIMARY KEY,
    tabla_afectada VARCHAR(100) NOT NULL,
    id_registro_afectado VARCHAR(100) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario INT,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);
