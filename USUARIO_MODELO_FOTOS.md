# URLs de foto en Usuario y ModeloVehiculo

- Usuario: propiedad JSON `fotoPerfilUrl`, columna `usuario.foto_perfil_url`.
- ModeloVehiculo: propiedad JSON `imagenUrl`, columna `modelo_vehiculo.imagen_url`.

Ambos campos son opcionales y admiten hasta 2048 caracteres. Aceptan una URL absoluta o una ruta relativa del servicio de imágenes. Solo se almacena la referencia, no el contenido binario.

## Actualización

Enviar el nuevo campo junto con los campos habituales requeridos por los PUT existentes:

- `PUT /api/usuarios/{id}` admite `fotoPerfilUrl`.
- `PUT /api/modelos-vehiculos/{id}` admite `imagenUrl`.

Omitir el campo o enviar `null` conserva la foto previa. Enviar una cadena vacía o solo espacios elimina la referencia. Las URLs recibidas se guardan sin espacios al inicio ni al final.

Los campos también se admiten al crear las entidades y se incluyen en sus respuestas de consulta, listado y actualización. El registro de usuarios y el login devuelven `fotoPerfilUrl`. Las contraseñas no se incluyen en las respuestas.

Los permisos de estos endpoints se conservan: administración de usuarios requiere ADMIN; gestión de modelos requiere ADMIN o EMPLEADO. Este cambio no añade un endpoint para que un cliente edite su propio perfil.

## Base de datos

El archivo `migraciones/20261114_fotos_usuario_modelo.sql` contiene los ALTER TABLE idempotentes. El backend local con `ddl-auto=update` también añade las columnas al arrancar sin eliminar registros. No usar `ddl-auto=create` para este cambio.
