# Perfil del cliente

Endpoints autenticados bajo `/api/perfil`:

- GET: datos del usuario identificado por el JWT; no acepta un ID de otro usuario.
- PUT: nombre, apellido, teléfono, dirección y fotoPerfilUrl. Usa PerfilRequest con validaciones. No modifica correo, contraseña, rol ni estado.
- POST `/foto`: multipart con campo `archivo`; valida la imagen con SupabaseStorageService, genera una ruta única en `perfiles` y actualiza la foto del usuario autenticado. Hasta 10 MB. Los archivos anteriores no se borran automáticamente.
- GET `/pedidos/{id}/items`: devuelve las líneas históricas solo si el pedido pertenece al usuario. Devuelve 404 para pedidos ajenos o inexistentes.

Los usuarios administrativos siguen utilizando sus endpoints actuales. El historial usa el endpoint existente `/api/ordenes/usuario/{id}`, que ya verifica la propiedad. No cambia los permisos de escritura del Storage general.

Pruebas: `mvn -Dtest=PerfilControllerTest,StorageControllerTest,SupabaseStorageServiceTest test`.

## Seguridad

- PATCH `/api/perfil/password`: `contrasenaActual`, `contrasenaNueva`, `confirmacionContrasena`. Requiere contraseña actual correcta, confirmación igual y contraseña nueva diferente, entre 8 y 72 caracteres y máximo 72 bytes UTF-8. Se guarda con PasswordEncoder. Devuelve ApiResponse sin datos.
- PATCH `/api/perfil/correo`: `correoNuevo`, `confirmacionCorreo`, `contrasenaActual`. Valida formato, confirmación, contraseña actual y unicidad. Normaliza el correo y devuelve JwtAuthResponse con token del correo nuevo. El frontend reemplaza la sesión inmediatamente. No envía un correo de verificación.

Ambos endpoints identifican la cuenta por el principal autenticado; no aceptan un usuarioId. El cambio de contraseña no revoca los JWT existentes. Se conservan los endpoints administrativos anteriores.

Pruebas de seguridad: `mvn -Dtest=PerfilControllerTest,UsuarioCredencialesTest test`.
