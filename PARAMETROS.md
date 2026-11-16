# Parámetros de tienda

Tabla `parametro_tienda`, registro único ID 1, persistido mediante JPA. ApplicationRunner crea el registro inicial únicamente si no existe y no sobrescribe cambios en reinicios. Incluye datos públicos del negocio y contacto: nombre, local, teléfono, WhatsApp internacional, dirección, correo, horario, latitud, longitud y enlace Google Maps. No almacenar claves ni credenciales aquí.

- GET `/api/parametros`: lectura pública.
- PUT `/api/parametros`: solo ADMIN; recibe todos los campos editables y la versión leída. La versión se gestiona con `@Version` y se devuelve 409 ante edición concurrente. EMPLEADO y CLIENTE no pueden guardar.

Validaciones: límites de texto, formato de teléfono/WhatsApp, correo, coordenadas válidas y enlace HTTPS de Google para Maps. El iframe del frontend se construye únicamente desde coordenadas.

Inicial: teléfono y ubicación que el usuario proporcionó. Dirección “Dirección por confirmar”; correo y horario vacíos hasta que ADMIN los complete. Hibernate usa ddl-auto=update del proyecto para crear la tabla sin recrear otras tablas.

Verificación: `mvn -Dtest=ParametrosControllerTest,ParametrosServiceTest test`.

Validaciones reforzadas: textos recortados antes de validar; mínimos de nombre/local/dirección, control de caracteres, teléfono con 7–15 dígitos, correo con dominio completo, horario opcional de 3–300 caracteres, coordenadas con hasta 7 decimales y URLs específicas de Maps. Se rechazan coordenadas no numéricas con HTTP 400. Las respuestas de validación incluyen mensajes de campo en español.
