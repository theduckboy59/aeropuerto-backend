# Cambios recientes (modelo de datos y migraciones)

Ultima actualizacion registrada: `d35495b` (2026-04-28) - "traje de nuevo el main".

## Resumen actual
- Se estandariza el modelo de datos para `Pasajero` y `Empleado`, junto con catalogos asociados.
- Se agregan migraciones Flyway para crear tablas e insertar catalogos iniciales.
- En este momento **no hay CRUD/REST implementado** en el repositorio para `User`/`Pasajero`/`Empleado` (carpetas `controller/`, `service/` y `repository/` sin clases).
- La aplicacion **si cuenta** con clase de arranque Spring Boot: `AeropuertolosprimosApplication`.

## Como correr el backend (lo que hay hoy)
### Requisitos
- Java 17
- PostgreSQL (local o remoto). Opcional: Docker para levantar PostgreSQL.

### Perfiles y variables de entorno
- Perfil activo por defecto: `dev` (ver `src/main/resources/application.yaml`).
- `dev`: `src/main/resources/application-dev.yml` usa `DB_PASSWORD`.
- `prod`: `src/main/resources/application-prod.yml` usa `DB_PASSWORD_AWS`.

### Comando
- PowerShell: `./mvnw.cmd spring-boot:run`

### Nota sobre Docker Compose
Existe `compose.yaml` con un servicio `postgres`, pero actualmente sus valores (`POSTGRES_DB`, `POSTGRES_USER`, etc.) **no coinciden** con `application-dev.yml` (que apunta a `aeropuerto_los_primos` / `postgres`). Ademas, en `compose.yaml` el puerto esta declarado como `'5432'` (sin `host:container`), por lo que Docker puede asignar un puerto host aleatorio.

## Lo que se agrego
- Modelos (JPA) de catalogos y entidades:
  - `Area`, `CodigoArea`, `Empleado`, `Licencia`, `Nacionalidad`, `NivelAcceso`, `Pasajero`, `Rol`, `TipoDocumento`, `TipoEmpleado`, `Turno`.
- Migraciones Flyway:
  - `V1__create_tables.sql`: crea `status_catalog` y `users` e inserta valores iniciales de estado.
  - `V2__create_pasajero.sql`: crea `tipo_documento`, `nacionalidad`, `codigo_area`, `pasajero` e inserta valores iniciales.
  - `V3__create_empleado.sql`: crea `tipo_empleado`, `turno`, `nivel_acceso`, `rol`, `area`, `licencia`, `empleado` e inserta valores iniciales (requiere tabla `aerolinea` existente, ya que `empleado.aerolinea_id` referencia `aerolinea(id)`).

## Lo que se elimino
- Se removio la capa REST/servicios previa de `User`:
  - `src/main/java/com/aeropuertolosprimos/backend/controller/UserController.java`.
  - `src/main/java/com/aeropuertolosprimos/backend/repository/UserRepository.java`.
  - `src/main/java/com/aeropuertolosprimos/backend/service/UserService.java`.
  - `src/main/java/com/aeropuertolosprimos/backend/service/UserServiceImpl.java`.

## Impacto
- El proyecto conserva el modelo `User` (clase), pero **sin endpoints** (no hay REST/CRUD implementado).
- Las migraciones crean las tablas base `users` y `status_catalog` mediante `V1`. En `V3`, la tabla `empleado` referencia `aerolinea(id)`, por lo que `aerolinea` debe existir previamente.

## Proximos pasos sugeridos
- Implementar endpoints de creacion/consulta (p. ej. `POST /pasajeros`, `POST /empleados`) y, si aplica, un registro combinado (`User` + `Pasajero`) en una transaccion.
- Definir una estrategia de autenticacion (por ejemplo: login en `"/auth/**"` y/o desactivar seguridad en `dev` mientras se construye el API).
- Alinear `compose.yaml` con `application-dev.yml` (nombre DB/usuario/puerto) para facilitar el levantado local.
