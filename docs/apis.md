# APIs (Backend) — estado actual

Base URL (dev): `http://localhost:8080`

Autenticación:
- Público: rutas bajo `/auth/**`.
- Protegido: cualquier otra ruta requiere JWT en `Authorization: Bearer <token>`.

## CORS (Frontend en 4200)
Para permitir llamadas desde Angular (por defecto `http://localhost:4200`) al backend (`http://localhost:8080`), el backend habilita CORS para:
- Origin permitido: `http://localhost:4200`
- Métodos: `GET, POST, PUT, DELETE, OPTIONS`
- Headers permitidos: `Authorization, Content-Type`
- Credentials: `true`

## Resumen de endpoints existentes

Públicos:
- `POST /auth/register`
- `POST /auth/login`

Protegidos (JWT requerido):
- `POST /pasajeros`
- `GET /pasajeros`
- `GET /pasajeros/{id}`
- `DELETE /pasajeros/{id}`

Catálogos (públicos):
- `GET /catalogos/status`
- `GET /catalogos/tipo-documento`
- `GET /catalogos/nacionalidad`
- `GET /catalogos/codigo-area`
- `GET /catalogos/aerolinea`
- `GET /catalogos/tipo-empleado`
- `GET /catalogos/turno`
- `GET /catalogos/nivel-acceso`
- `GET /catalogos/rol`
- `GET /catalogos/area`
- `GET /catalogos/licencia`

## Auth (`/auth`)

### `POST /auth/register` (público)
Controller: `AuthController`

Request body: `RegisterRequest`
- `username` (string)
- `email` (string)
- `password` (string)
- `tipoDocumentoId` (integer)
- `numeroDocumento` (string)
- `nombreCompleto` (string)
- `fechaNacimiento` (date, `YYYY-MM-DD`)
- `nacionalidadId` (integer)
- `codigoAreaId` (integer)
- `telefono` (string)
- `telefonoEmergencia` (string)
- `direccion` (string)

Response body:
- `200 OK` (string): `"Usuario creado correctamente"`

### `POST /auth/login` (público)
Controller: `AuthController`

Request body: `LoginRequest`
- `email` (string) — puede ser email o username
  - También acepta `username` como alias del campo `email` (para compatibilidad con front).
- `password` (string)

Response body: `LoginResponse`
- `200 OK`: `{ "token": "<jwt>" }`

## Pasajeros (`/pasajeros`) (protegido)

### `POST /pasajeros`
Controller: `PasajeroController`

Request body: `Pasajero`
- `userId` (integer)
- `tipoDocumentoId` (integer)
- `numeroDocumento` (string)
- `nombreCompleto` (string)
- `fechaNacimiento` (date, `YYYY-MM-DD`)
- `nacionalidadId` (integer)
- `codigoAreaId` (integer)
- `telefono` (string)
- `telefonoEmergencia` (string)
- `direccion` (string)
- `estadoId` (integer, opcional)

Response body:
- `200 OK`: `Pasajero` (incluye `id`, `createdAt`, `updatedAt`, etc.)

### `GET /pasajeros`
Controller: `PasajeroController`

Response body:
- `200 OK`: lista de `Pasajero`

### `GET /pasajeros/{id}`
Controller: `PasajeroController`

Path params:
- `id` (integer)

Response body:
- `200 OK`: `Pasajero`

### `DELETE /pasajeros/{id}`
Controller: `PasajeroController`

Path params:
- `id` (integer)

Response body:
- `200 OK` (sin body)

## Catálogos (`/catalogos`) (público)

Estos catálogos existen como tablas (Flyway) y como modelos JPA en `src/main/java/com/aeropuertolosprimos/backend/model`, y se exponen vía `GET /catalogos/*` (sin JWT).

Endpoints:
- `GET /catalogos/status` → lista de `StatusCatalog` (`id`, `name`)
- `GET /catalogos/tipo-documento` → lista de `TipoDocumento` (`id`, `nombre`)
- `GET /catalogos/nacionalidad` → lista de `Nacionalidad` (`id`, `nombre`)
- `GET /catalogos/codigo-area` → lista de `CodigoArea` (`id`, `codigo`)
- `GET /catalogos/aerolinea` → lista de `Aerolinea` (`id`, `nombre`, `estado`)
- `GET /catalogos/tipo-empleado` → lista de `TipoEmpleado` (`id`, `nombre`)
- `GET /catalogos/turno` → lista de `Turno` (`id`, `nombre`)
- `GET /catalogos/nivel-acceso` → lista de `NivelAcceso` (`id`, `nombre`)
- `GET /catalogos/rol` → lista de `Rol` (`id`, `nombre`)
- `GET /catalogos/area` → lista de `Area` (`id`, `nombre`)
- `GET /catalogos/licencia` → lista de `Licencia` (`id`, `nombre`)

Catálogos creados en migraciones:

- `status_catalog`
  - Campos: `id`, `name`
  - Seeds: `ACTIVO`, `INACTIVO`
- `tipo_documento`
  - Campos: `id`, `nombre`
  - Seeds: `DPI`, `PASAPORTE`, `LICENCIA`
- `nacionalidad`
  - Campos: `id`, `nombre`
  - Seeds: `GUATEMALA`, `EL SALVADOR`, `MEXICO`
- `codigo_area`
  - Campos: `id`, `codigo`
  - Seeds: `+502`, `+503`, `+52`

Catálogos de empleados (migración `V3__create_empleado.sql`):

- `aerolinea`
  - Campos: `id`, `nombre`, `estado`
  - Seeds: `AEROLINEA DEMO` (estado `ACTIVA`)
- `tipo_empleado`
  - Campos: `id`, `nombre`
  - Seeds: `PILOTO`, `COPILOTO`, `CABINA`
- `turno`
  - Campos: `id`, `nombre`
  - Seeds: `MATUTINO`, `VESPERTINO`, `NOCTURNO`
- `nivel_acceso`
  - Campos: `id`, `nombre`
  - Seeds: `ADMIN`, `SUPERVISOR`, `OPERATIVO`
- `rol`
  - Campos: `id`, `nombre`
  - Seeds: `ADMIN`, `CHECKIN`, `OPERADOR`
- `area`
  - Campos: `id`, `nombre`
  - Seeds: `OPERACIONES`, `CABINA`, `TI`
- `licencia`
  - Campos: `id`, `nombre`
  - Seeds: `COMERCIAL`, `PRIVADA`, `INSTRUCTOR`

## Empleados — modelo y BD, sin API REST actualmente

Existe la entidad `Empleado` y la tabla `empleado` (Flyway), pero **no hay endpoints** REST implementados.

Campos principales de `Empleado` (tabla `empleado`):
- `userId`, `tipoEmpleadoId`, `aerolineaId`, `codigoEmpleado`, `nombreCompleto`
- `fechaIngreso`, `fechaSalida`
- `turnoId`, `nivelAccesoId`, `rolId`, `areaId`
- `licenciaId`, `fechaVencimientoLicencia`
- `estadoId`, `createdAt`, `updatedAt`

## OpenAPI / Swagger (si está habilitado por dependencia Springdoc)
- `GET /v3/api-docs`
- `GET /swagger-ui/index.html`
