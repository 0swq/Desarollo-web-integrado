# Imágenes con Supabase Storage

El backend almacena las imágenes en un bucket dedicado de Supabase y las entrega por streaming HTTP. La clave de servidor permanece en `backend/.env`, fuera del repositorio. No requiere añadir dependencias al proyecto.

## Configuración

En `backend/.env`:

```properties
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_STORAGE_KEY=tu_clave_de_servidor
SUPABASE_STORAGE_BUCKET=autopartes-imagenes
```

Usar una **secret key** (`sb_secret_...`) o una clave legacy `service_role`. Las nuevas secret keys se envían en `apikey`; únicamente las legacy también usan `Authorization: Bearer`. No usar una clave publishable, un token personal `sbp_...`, el JWT de Autopartes ni la contraseña de PostgreSQL.

El servicio no hace llamadas de red al arrancar. Si falta configuración, solo las operaciones de Storage devuelven 503; el resto del backend puede funcionar.

La primera carga comprueba el bucket y, si no existe, lo crea **privado**, limitado a 10 MiB y JPEG, PNG, WebP y GIF. Si ya existe, no cambia sus permisos ni restricciones. Para gestionar el bucket manualmente, configurar `app.storage.supabase.create-bucket=false`. Usar un bucket dedicado a imágenes públicas del catálogo: aunque el bucket sea privado, la ruta de lectura del backend es pública.

## Cuatro operaciones

| Método | Endpoint | Permisos | Resultado |
|---|---|---|---|
| POST | `/api/storage/imagenes` | ADMIN o EMPLEADO | Crea una imagen con UUID único. Multipart `archivo`; parámetro `carpeta=productos` (por defecto) o `categorias`; también se admite `modelos`. |
| PUT | `/api/storage/imagenes/{ruta}` | ADMIN o EMPLEADO | Reemplaza el contenido conservando ruta y formato. Semántica upsert: crea el objeto si no existía. Multipart `archivo`. |
| GET | `/api/storage/imagenes/{ruta}` | Público | Imagen binaria por streaming. |
| DELETE | `/api/storage/imagenes/{ruta}` | ADMIN o EMPLEADO | Elimina el objeto del storage. |

Ejemplo de respuesta de carga:

```json
{
  "success": true,
  "message": "Imagen cargada",
  "data": {
    "ruta": "productos/550e8400-e29b-41d4-a716-446655440000.png",
    "url": "/api/storage/imagenes/productos/550e8400-e29b-41d4-a716-446655440000.png",
    "contentType": "image/png",
    "size": 12345
  }
}
```

Guardar `data.url` en `imagenUrl` del producto o categoría mediante su endpoint existente. El frontend puede usarla en `<img src="...">`. La URL es relativa al backend: en desarrollo funciona mediante el proxy `/api` de Angular; en despliegue, servir `/api` con un proxy inverso o resolver la URL contra el origen del backend.

El almacenamiento de archivos y la actualización de entidades son operaciones separadas. Eliminar un archivo no elimina productos ni limpia automáticamente `imagenUrl`. Sobrescribir conserva el enlace. Para reemplazar con otro formato, cargar un archivo nuevo y actualizar la entidad.

## Streaming y validaciones

- La descarga copia el stream remoto a la respuesta en bloques de 16 KiB, sin convertir toda la imagen a `byte[]`, base64 ni un archivo temporal local.
- Reenvía un único `Range: bytes=...` y conserva HTTP 206 y `Content-Range` si Supabase los devuelve. Rangos múltiples se rechazan con 416.
- Reenvía `If-None-Match`, conserva `ETag` y `Last-Modified`, y devuelve 304 sin cuerpo cuando corresponde. Usa `Cache-Control: public, no-cache` para revalidar sobrescrituras.
- La carga usa un stream y el tamaño multipart declarado; no llama a `MultipartFile.getBytes()`.
- Rechaza archivos vacíos, tamaños superiores a 10 MiB, rutas externas o con traversal, SVG y formatos no admitidos. Comprueba la firma del formato y su consistencia con el MIME declarado. Esto no sustituye una validación de dimensiones, decodificación completa o análisis antimalware.
- No se sigue ninguna redirección del proveedor para evitar reenviar credenciales a otro host. Las conexiones tienen límites de espera de conexión y lectura.
- Las rutas están limitadas a `productos/nombre.ext` y `categorias/nombre.ext` y `modelos/nombre.ext`.
- Los errores del proveedor se convierten en mensajes controlados; nunca se devuelve su cuerpo privado ni la clave.

Swagger: `http://localhost:8080/swagger-ui.html`, sección **Storage**. Autorizar con un JWT de Autopartes de ADMIN/EMPLEADO para probar escrituras.

## Arranque y pruebas

Usar `spring.jpa.hibernate.ddl-auto=update` o `validate` al reiniciar. No iniciar con `create` porque borraría los datos existentes. La configuración de ejemplo usa `update`.

Pruebas con un servidor HTTP simulado y MockMvc:

```text
mvn -Dtest=SupabaseStorageServiceTest,StorageControllerTest test
```

Fuentes del protocolo REST:

- https://supabase.com/docs/guides/getting-started/api-keys
- https://supabase.com/docs/guides/storage/security/access-control
- https://supabase.com/docs/guides/storage/serving/downloads
- https://supabase.github.io/storage/

Prueba real opcional (crea una imagen temporal, comprueba las cuatro operaciones y la elimina):

mvn -Dtest=SupabaseStorageLiveTest -Dstorage.live=true test

Esta prueba usa las credenciales de backend/.env y crea el bucket configurado si no existe.


## Imágenes de marcas

La carpeta marcas también está admitida por el servicio de Storage. MarcaVehiculo guarda imagenUrl, incluida en creación, actualización y respuestas. Los diez logotipos de ejemplo y sus fuentes están documentados en datos-ejemplo/marcas-imagenes.
