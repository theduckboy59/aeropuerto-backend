# Integración de registro: `User` y `Pasajero`

Resumen:
- El sistema actualmente tiene un CRUD para `User`.
- Los registros de "pasajeros" deberán usar la clase `User` para los datos de cuenta/registro.
- Cuando un usuario se registre, deberá completarse tanto la tabla `users` como la tabla `pasajero` con los campos correspondientes.
- En consecuencia, el CRUD actual de `User` quedará obsoleto para el flujo de registro combinado (no hacer cambios de código ahora).

Detalles operativos esperados:
- Flujo de registro combinado:
  - Recibir datos requeridos por `users` (por ejemplo: email, password, roles, etc.).
  - Recibir datos requeridos por `pasajero` (por ejemplo: tipo_documento_id, numero_documento, nombre_completo, fecha_nacimiento, nacionalidad_id, telefono, direccion, etc.).
  - En una única operación transaccional, crear el `User` y luego crear el `Pasajero` asociado con `user_id` apuntando al `User` creado.
  - Validaciones: unicidad de email/numero_documento, formatos, y campos obligatorios.

Notas y decisiones actuales:
- No se modifica el código existente en este momento.
- El `UserController` y los servicios existentes siguen en el repo, pero el flujo de registro futuro deberá delegar a un nuevo controlador/servicio o extender el existente para crear ambas entidades en una transacción.

Siguientes pasos sugeridos (opcionales):
- Implementar un endpoint `POST /register` que reciba un DTO con campos de `User` + `Pasajero`.
- Implementar un servicio que ejecute la creación de `User` y `Pasajero` en una transacción.
- Añadir pruebas de integración para el flujo de registro.

Registrado por: nota del desarrollador solicitada por el equipo.
