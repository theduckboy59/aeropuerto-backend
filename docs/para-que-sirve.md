ahora # ¿Para qué sirve cada endpoint?

Base URL (dev): `http://localhost:8080`

Autenticación:
- Público: rutas bajo `/auth/**` y `/catalogos/**`.
- Protegido: rutas como `/pasajeros/**` requieren JWT en `Authorization: Bearer <token>`.

Documentación automática (Springdoc / Swagger):
- `GET /v3/api-docs` devuelve el JSON OpenAPI.
- `GET /swagger-ui/index.html` muestra la UI interactiva.

## Auth (`/auth`)

### `POST /auth/register`
Sirve para **crear un usuario** en el sistema (registro).

Entrada: `RegisterRequest` (datos del usuario como `username`, `email`, `password`, documento, teléfono, dirección, etc.).

Salida:
- `200 OK` (texto): `"Usuario creado correctamente"`.

### `POST /auth/login`
Sirve para **iniciar sesión** y obtener un **token JWT** para consumir endpoints protegidos.

Entrada: `LoginRequest` (`email` o `username`, y `password`).

Salida:
- `200 OK` (JSON): `{ "token": "<jwt>" }`.

## Pasajeros (`/pasajeros`) (protegido con JWT)

### `POST /pasajeros`
Sirve para **crear un pasajero** (guardar sus datos en BD).

Entrada: `Pasajero` (por ejemplo: `tipoDocumentoId`, `numeroDocumento`, `nombreCompleto`, `fechaNacimiento`, `nacionalidadId`, `telefono`, `direccion`, etc.).

Salida:
- `200 OK` (JSON): el `Pasajero` creado (incluye su `id`).

### `GET /pasajeros`
Sirve para **listar todos los pasajeros** registrados.

Salida:
- `200 OK` (JSON): lista de `Pasajero`.

### `GET /pasajeros/{id}`
Sirve para **obtener un pasajero por id**.

Parámetro de ruta:
- `id` (integer): id del pasajero.

Salida:
- `200 OK` (JSON): `Pasajero`.

### `DELETE /pasajeros/{id}`
Sirve para **eliminar un pasajero por id**.

Parámetro de ruta:
- `id` (integer): id del pasajero.

Salida:
- `200 OK` sin body.

## Catálogos (`/catalogos`) (público)

Sirve para **cargar listas “maestras”** (catálogos) desde BD: tipos de documento, nacionalidades, áreas, roles, etc. Normalmente el frontend los usa para poblar selects/combos.

### `GET /catalogos/status`
Sirve para listar **estados** (ej: ACTIVO/INACTIVO).

### `GET /catalogos/tipo-documento`
Sirve para listar **tipos de documento** (ej: DPI/PASAPORTE/LICENCIA).

### `GET /catalogos/nacionalidad`
Sirve para listar **nacionalidades**.

### `GET /catalogos/codigo-area`
Sirve para listar **códigos de país/área** (ej: +502).

### `GET /catalogos/aerolinea`
Sirve para listar **aerolíneas**.

### `GET /catalogos/tipo-empleado`
Sirve para listar **tipos de empleado** (ej: PILOTO).

### `GET /catalogos/turno`
Sirve para listar **turnos** (ej: MATUTINO).

### `GET /catalogos/nivel-acceso`
Sirve para listar **niveles de acceso** (permisos).

### `GET /catalogos/rol`
Sirve para listar **roles** (ej: ADMIN, CHECKIN).

### `GET /catalogos/area`
Sirve para listar **áreas** (departamentos).

### `GET /catalogos/licencia`
Sirve para listar **licencias** (ej: COMERCIAL, PRIVADA).

