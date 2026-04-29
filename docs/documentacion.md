# Cambios recientes (modelo de datos y migraciones)

Ultima actualizacion registrada: `c024ac2` (2026-04-28) - "modelos base `User`, `Pasajero` y `Empleado` con catalogos y migraciones Flyway".

## Resumen actual
- Se estandariza el modelo de datos para `Pasajero` y `Empleado`, junto con catalogos asociados.
- Se agregan migraciones Flyway para crear tablas e insertar catalogos iniciales.
- En este momento **no hay CRUD/REST implementado** en el repositorio para `User`/`Pasajero`/`Empleado` (se removieron controladores/servicios anteriores).

## Lo que se agrego
- Modelos (JPA) de catalogos y entidades:
  - `Area`, `CodigoArea`, `Empleado`, `Licencia`, `Nacionalidad`, `NivelAcceso`, `Pasajero`, `Rol`, `TipoDocumento`, `TipoEmpleado`, `Turno`.
- Migraciones Flyway:
  - `V2__create_pasajero.sql`: crea `tipo_documento`, `nacionalidad`, `codigo_area`, `pasajero` e inserta valores iniciales.
  - `V3__create_empleado.sql`: crea `tipo_empleado`, `turno`, `nivel_acceso`, `rol`, `area`, `licencia`, `empleado` e inserta valores iniciales.

## Lo que se elimino
- Se removio la capa REST/servicios previa de `User`:
  - `src/main/java/com/aeropuertolosprimos/backend/AeropuertolosprimosApplication.java`.
  - `src/main/java/com/aeropuertolosprimos/backend/controller/UserController.java`.
  - `src/main/java/com/aeropuertolosprimos/backend/repository/UserRepository.java`.
  - `src/main/java/com/aeropuertolosprimos/backend/service/UserService.java`.
  - `src/main/java/com/aeropuertolosprimos/backend/service/UserServiceImpl.java`.

## Impacto
- El proyecto actualmente conserva el modelo `User` (clase), pero **sin endpoints** ni clase de arranque Spring Boot.
- Las migraciones referencian tablas existentes (`users`, `status_catalog`, `aerolinea`), por lo que se asume que provienen de migraciones previas (`V1...`) o de una base ya inicializada.

## Proximos pasos sugeridos
- Reincorporar la clase `@SpringBootApplication` (main) para poder levantar el backend.
- Implementar endpoints de creacion/consulta (p. ej. `POST /pasajeros`, `POST /empleados`) y, si aplica, un registro combinado (`User` + `Pasajero`) en una transaccion.
