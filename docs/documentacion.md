# Documentacion del proyecto (estado actual)

Backend Spring Boot (Java 17) con PostgreSQL, Flyway, Spring Security y JWT (JJWT).

## Funcionamiento general
- La app levanta en Tomcat embebido y expone APIs HTTP (Spring Web).
- La base de datos se prepara/actualiza con Flyway al iniciar (scripts en `src/main/resources/db/migration`).
- La seguridad se basa en Spring Security:
  - `"/auth/**"` es publico.
  - El resto de rutas requiere un JWT valido y no expirado en el header `Authorization: Bearer <token>`.

## Configuracion (propiedades y variables)
Archivos:
- `src/main/resources/application.yaml` (perfil activo y propiedades JWT)
- `src/main/resources/application-dev.yml` (conexion local)
- `src/main/resources/application-prod.yml` (conexion AWS)

Variables de entorno usadas:
- `DB_PASSWORD` (perfil `dev`)
- `DB_PASSWORD_AWS` (perfil `prod`)
- `JWT_SECRET` (se usa como `jwt.secret`)

JWT:
- `jwt.expiration` esta configurado en `3600000` ms (1 hora).

## Dependencias relevantes (pom.xml)
- Spring Web, Spring Data JPA, Validation
- PostgreSQL driver
- Flyway (`flyway-core` + `flyway-database-postgresql`)
- Spring Security
- JJWT (`jjwt-api` + `jjwt-impl` runtime + `jjwt-jackson` runtime)

## Migraciones (Flyway)
- `V1__create_tables.sql`: crea `status_catalog` y `users` (+ inserts base).
- `V2__create_pasajero.sql`: crea catalogos `tipo_documento`, `nacionalidad`, `codigo_area` y tabla `pasajero` (+ inserts base).
- `V3__create_empleado.sql`: crea `aerolinea`, catalogos de empleado y tabla `empleado` (+ inserts base).

## APIs disponibles
Base URL (dev): `http://localhost:8080`

### AuthController (`/auth`)
Archivo: `src/main/java/com/aeropuertolosprimos/backend/controller/AuthController.java`

- `POST /auth/register`
  - Crea un `User` y un `Pasajero` en una misma transaccion.
  - Request body (DTO `RegisterRequest`):
    - `username`, `email`, `password`
    - `tipoDocumentoId`, `numeroDocumento`, `nombreCompleto`, `fechaNacimiento`, `nacionalidadId`, `codigoAreaId`, `telefono`, `telefonoEmergencia`, `direccion`
  - Respuesta: `"Usuario creado correctamente"`

- `POST /auth/login`
  - Autentica con `email` o `username` + `password` y retorna un token JWT. Se envia en el campo `email` del `LoginRequest` (puede ser email o username).
  - Request body (DTO `LoginRequest`): `email`, `password`
  - Response body (DTO `LoginResponse`): `{ "token": "<jwt>" }`

Ejemplo probado con Postman:
- `POST http://localhost:8080/auth/register`
  - Body:
    - `{"username":"luis","email":"luis@test.com","password":"123456","tipoDocumentoId":1,"numeroDocumento":"88888888","nombreCompleto":"Luis Sipac","fechaNacimiento":"2000-01-01","nacionalidadId":1,"codigoAreaId":1,"telefono":"12345678","telefonoEmergencia":"87654321","direccion":"Guatemala, Ciudad de Guatemala"}`

### PasajeroController (`/pasajeros`)
Archivo: `src/main/java/com/aeropuertolosprimos/backend/controller/PasajeroController.java`

Rutas (requieren autenticacion segun `SecurityConfig`):
- `POST /pasajeros` (crea pasajero)
- `GET /pasajeros` (lista)
- `GET /pasajeros/{id}` (obtiene por id)
- `DELETE /pasajeros/{id}` (elimina)

## Clases, interfaces y responsabilidades

### Seguridad/JWT
- `SecurityConfig`: define reglas (publico `"/auth/**"`) y agrega `JwtFilter` al chain.
- `JwtFilter`: lee `Authorization: Bearer ...`, valida token y coloca autenticacion en el `SecurityContext`.
- `JwtService`: genera token (`generateToken`) y extrae subject (`extractUsername`) usando `jwt.secret` y `jwt.expiration`.
- `CustomUserDetailsService`: carga usuario desde BD por `email` o `username` para el proceso de login.

## Flujo de autenticacion
1. El usuario se registra en `POST /auth/register`
2. Hace login en `POST /auth/login`
3. Recibe un JWT
4. Envia el token en `Authorization: Bearer <token>`
5. El `JwtFilter` valida el token y registra al usuario en el `SecurityContext`

### Servicios
- `AuthService` / `AuthServiceImpl`:
  - `register(RegisterRequest)`: valida email unico, guarda `User` (password con BCrypt) y crea `Pasajero` asociado.
- `PasajeroService` / `PasajeroServiceImpl`:
  - `crear`, `listar`, `obtenerPorId`, `eliminar`.

### Repositorios (JPA)
- `UserRepository`: `findByEmail`, `findByUsername`.
- `PasajeroRepository`: `findByNumeroDocumento`.

### Modelos (JPA)
Entidades principales:
- `User` (tabla `users`)
- `Pasajero` (tabla `pasajero`)
- `Empleado` (tabla `empleado`)
Catalogos:
- `StatusCatalog`, `TipoDocumento`, `Nacionalidad`, `CodigoArea`, `TipoEmpleado`, `Turno`, `NivelAcceso`, `Rol`, `Area`, `Licencia`

## Estado verificado por log (ultima ejecucion)
App levanto [OK]
Flyway ejecutado [OK]
Conexion a PostgreSQL [OK]
Tomcat en 8080 [OK]
Sin errores de JWT [OK]

## Pendientes (segun codigo actual)
- Roles/permisos reales (hoy el `JwtFilter` setea autenticacion basica en el `SecurityContext`, sin roles aun).
- Endpoints para `Empleado` y catalogos (solo existen modelos/migraciones).

## Estado actual del sistema
- Autenticacion JWT funcional [OK]
- Registro de usuarios y pasajeros [OK]
- CRUD de pasajeros [OK]
- Seguridad en endpoints protegidos [OK]

Nivel actual: Backend listo para integracion con frontend

## Qué te falta (mínimo)
Para que el front funcione bien necesitas:

Endpoints REST (ejemplo):
- `/auth/login` (existe)
- `/vuelos` (no existe en el backend actual)
- `/tripulacion` (no existe en el backend actual)
- `/pasajeros` (existe)

DTOs claros (request/response).
