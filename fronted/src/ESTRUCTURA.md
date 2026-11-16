# Organización del frontend



## Páginas



Cada página tiene su propia carpeta. `administracion`, `pago` y `perfil` son áreas hermanas dentro de `app/pages`.



```text

app/pages/

├── administracion/

│   ├── administracion.routes.ts

│   ├── panel/                # Layout y navegación del área

│   ├── usuarios/

│   ├── productos/            # Antes gestion-productos

│   ├── stock/                # Antes gestion-stock

│   ├── proveedores/

│   ├── marcas/

│   ├── categorias/

│   ├── modelos/

│   ├── compatibilidades/

│   ├── pedidos/

│   └── pagos/

├── pago/

│   ├── pago.routes.ts

│   ├── pago-layout.ts/html/css

│   ├── llenar-datos/

│   ├── pago/

│   └── confirmacion/

├── perfil/

│   ├── perfil.routes.ts

│   ├── perfil-layout.ts/html/css

│   ├── mi-perfil/

│   ├── mis-pedidos/

│   └── seguridad/

├── auth/

│   ├── iniciar-sesion/

│   └── registro/

├── catalogo/

│   ├── listado-productos/

│   └── producto/

├── carrito/

└── inicio/

```



Las páginas nuevas son componentes standalone con sus archivos TypeScript, HTML y CSS. Son bases de pantalla con título y descripción; la reorganización no implementa formularios, operaciones CRUD, historial de pedidos ni procesamiento de pagos.



Las páginas existentes de productos y stock mantienen sus archivos `manage-products.*` y `manage-stock.*` dentro de las carpetas nuevas. Sus clases y pruebas se conservan. El catálogo, login, registro y carrito mantienen sus ubicaciones y URLs anteriores.



## Rutas y permisos



- `/administracion` abre `/administracion/productos`. Los módulos de gestión requieren ADMIN o EMPLEADO; `/administracion/usuarios` requiere ADMIN.

- `/pago` abre `/pago/llenar-datos`. Sus otras páginas son `/pago/pago` y `/pago/confirmacion`.

- `/perfil` abre `/perfil/mi-perfil`. Sus otras páginas son `/perfil/mis-pedidos` y `/perfil/seguridad`.

- Pago y perfil requieren sesión. Los guards se aplican también al navegar entre páginas del área.



Cada área declara sus rutas en su propio archivo y se carga bajo demanda desde `app.routes.ts`. Los layouts contienen la navegación del área y un `router-outlet` para sus páginas.



## Carpetas compartidas



- `app/_helpers`: guards de sesión/roles e interceptor HTTP.

- `app/_services`: servicios del backend y almacenamiento de sesión.

- `app/_shared/componentes`: navbar, footer, buscador compartido y tarjeta de producto.

- `app/models`: interfaces y roles del backend.

- `environments`: configuración pública incluida en el navegador; no almacenar claves privadas.



Durante el desarrollo, `/api` se redirige al backend mediante `proxy.conf.json`. Los permisos y validaciones también se verifican en el backend.



## Detalle público de producto



La página de app/pages/catalogo/producto está disponible en /catalogo/producto/:id. Las tarjetas compartidas enlazan a ella por UUID. Incluye imagen, precio, stock, ficha, compatibilidades y acceso al carrito autenticado. ProductoDetalleService consulta los endpoints reales y muestra un aviso si faltan datos adicionales. El login acepta returnUrl para regresar al producto.



## Selectores visuales del catálogo



El componente compartido vehicle-picker reemplaza los selectores nativos de marca y modelo por un diálogo con búsqueda local de 350 ms y tarjetas con imagen. Permite quitar la selección, cerrar con Escape o clic fuera y conserva el foco nativo del diálogo. El modelo elegido selecciona su marca; las opciones de modelos respetan marca y tipo de vehículo. La imagen de marca se obtiene del campo imagenUrl del backend.





## Nosotros, contacto y carrito



- `app/pages/nosotros` es una página pública junto a `inicio`, disponible en `/nosotros`. Contiene mapa de Google Maps y contacto por WhatsApp; `#ubicacion` abre la sección del local.

- `app/_shared/config/contacto.ts` centraliza el nombre, número y enlaces del negocio para Nosotros, footer y carrito.

- El footer adapta Mi cuenta a la sesión y enlaza Información con Nosotros, ubicación y WhatsApp.

- `/carrito` muestra información real de cada producto y permite actualizar cantidades, eliminar líneas y vaciar el carrito. Los importes se vuelven a consultar al backend tras cada cambio. Continuar enlaza con la página existente `/pago/llenar-datos`.

- Verificación del carrito: `npx ng test --watch=false --include=src/app/pages/carrito/cart-view.spec.ts --ts-config=tsconfig.cart-check.json`.



## Márgenes de páginas



`styles.css` define `--page-max-width` (1440 px), `--page-gutter` (16–36 px según la pantalla) y `--page-section-inset` para secciones de ancho completo. Inicio, catálogo, producto, carrito, Nosotros y los layouts de administración, perfil y pago comparten esta alineación con navbar y footer. Los formularios de acceso conservan su ancho compacto.



## Perfil en modal

El header y el footer abren `PerfilLayout` como diálogo sobre la página actual. El parámetro `perfil=mi-perfil`, `perfil=mis-pedidos` o `perfil=seguridad` conserva filtros, fragmento e historial: Atrás y Adelante restauran el modal. Se cierra con Escape, botón o clic fuera. Las rutas antiguas `/perfil/...` redirigen a `/inicio?perfil=...`.

- Mi perfil: consulta real, edición de nombre, apellido, teléfono y dirección, carga de foto a Supabase Storage o URL, quitar foto, validaciones y descarte de cambios. Actualiza los datos de sesión y el avatar del header. La foto cargada se guarda de inmediato; el formulario se guarda al confirmar. Correo de solo lectura.
- Mis pedidos: historial real ordenado por fecha, búsqueda por número, filtro por estado, cinco pedidos por página, detalle de líneas con los nombres y precios guardados en el pedido, dirección, teléfono, notas, subtotal, IGV y total. Consulta por WhatsApp con número de pedido.
- Seguridad: cambio de contraseña y correo mediante formularios con contraseña actual y confirmación del nuevo dato. Contraseñas de 8 a 72 caracteres (máximo 72 bytes UTF-8), errores del servidor, bloqueo de envíos simultáneos y limpieza tras guardar. El correo se valida y su cambio renueva y guarda el JWT antes de continuar.

`PerfilService` consume `/api/perfil`, `/api/perfil/foto`, `/api/ordenes/usuario/:id` y `/api/perfil/pedidos/:id/items`. Estados de carga, vacío, error y reintento. No se crean pedidos de ejemplo ni se cambian estados desde el historial del cliente.

Pruebas: `npx ng test --watch=false --include=src/app/pages/perfil/*.spec.ts --ts-config=tsconfig.perfil-check.json`.


## Flujo de compra

`pages/pago` contiene tres pasos: `llenar-datos`, `pago` y `confirmacion`, con layout y resumen de compra compartidos. `CheckoutService` consulta el carrito, guarda el borrador de entrega en sessionStorage separado por usuario, crea el pedido y consulta/simula su pago. La URL `?pedido=UUID` permite recargar, revisar o reintentar el mismo pedido. `PerfilService` precarga los datos de contacto.

La pasarela es un simulador explícito: no recoge tarjetas ni realiza cobros. Permite aprobar o rechazar un pago y reintentar un rechazo sin crear otro pedido. La simulación sí genera pedidos y descuenta stock; los pedidos se identifican con prefijo SIM- y pago SIMULADO. La confirmación utiliza el resultado real del backend. La creación notifica al contador del carrito. No se prometen correos de confirmación, fechas de entrega ni envío gratuito. El resumen aplica el IGV del 18% que calcula el backend; entrega por coordinar.

Pruebas: `npx ng test --watch=false --include=src/app/pages/pago/checkout.spec.ts --ts-config=tsconfig.checkout-check.json`.

## Acceso y registro

Las rutas públicas son `/iniciar-sesion`, `/registro` y `/recuperar-contrasena`. `/login` y `/register` redirigen a sus equivalentes en español. Los enlaces y guards usan las rutas nuevas; los endpoints del backend `/api/auth/login` y `/api/auth/register` se conservan.

El registro incluye nombre, apellido, correo, teléfono opcional, dirección opcional, contraseña y confirmación. No expone rol, estado, ID ni fechas. La foto por archivo se carga desde el perfil después de iniciar sesión. Confirmación de contraseña validada a nivel de formulario, prevención de doble envío y retorno a la compra tras registrarse e ingresar.

Olvidé mi contraseña abre una página de ayuda por WhatsApp: no hay recuperación automática por correo implementada en el backend y no se simula un envío de email.

Pruebas: `npx ng test --watch=false --include=src/app/pages/auth/**/*.spec.ts --ts-config=tsconfig.auth-pages-check.json`.

Las pantallas de acceso usan `data.acceso` en sus rutas: el layout principal sustituye el navbar por logo y enlace de regreso a la tienda y activa la variante compacta del footer. Aplica a iniciar sesión, registro y recuperación de acceso. El resto de páginas conserva el header y footer completos.


## Layout de administración

El área `/administracion` usa metadatos de ruta para ocultar el navbar y footer de la tienda. `AdminLayout` incluye los componentes compartidos `sidebar-admin` y `footer-admin`, con contenido lateral y menú adaptable a móvil.

- Gestión (ADMIN y EMPLEADO): productos, stock, categorías, marcas, modelos, compatibilidades, proveedores, pedidos y pagos.
- Admin (solo ADMIN): usuarios, reportes y parámetros. La sección completa se oculta a empleados.
- `/administracion/reportes` contiene la página inicial de reportes, con accesos a registros; no calcula todavía reportes consolidados.
- `/administracion/parametros` permite editar los parámetros persistidos de contacto, negocio y ubicación.

Las rutas de usuarios, reportes y parámetros tienen roles exclusivos de ADMIN y el guard se ejecuta también al navegar entre páginas o entrar por URL. El sidebar incluye acceso al perfil, regreso a la tienda y cierre de sesión.

Pruebas: `npx ng test --watch=false --include=src/app/pages/administracion/administracion.spec.ts --ts-config=tsconfig.admin-layout-check.json`.


## Parámetros persistidos de la tienda

`ParametrosService` reemplaza la antigua constante de contacto. Carga `/api/parametros` al iniciar el sitio y mantiene una señal compartida. Guardar desde el formulario de administración actualiza inmediatamente los componentes de la pestaña; otras visitas cargan los valores persistidos. El formulario incluye nombre comercial, establecimiento, teléfono visible, WhatsApp internacional, dirección, correo, horario, coordenadas y enlace del mapa, con vista previa. Se respeta la versión del registro para evitar sobrescribir otra edición.

Consumidores: inicio, marca del catálogo, etiquetas de logos, footer normal/compacto/admin, Nosotros, mapa, enlaces de WhatsApp del carrito, consultas de pedidos, confirmación y recuperación de acceso. Las coordenadas generan el iframe de Google y Cómo llegar; el enlace del establecimiento es opcional. La imagen del logo se conserva como recurso gráfico.

La dirección inicial dice “Dirección por confirmar”; no se ha inventado una dirección. No hay teléfonos ni coordenadas de respaldo fijos en el frontend. Si falla la lectura inicial, los enlaces no se sustituyen por datos antiguos; el administrador puede recargar el formulario.

Pruebas del cambio y consumidores: `npx ng test --watch=false --include=src/app/pages/administracion/parametros/parametros.spec.ts --ts-config=tsconfig.parametros-check.json`.

Validación de parámetros: textos normalizados, nombre/establecimiento de al menos 2 caracteres, dirección de al menos 5, teléfono con 7–15 dígitos (hasta 30 caracteres con formato), WhatsApp internacional de 7–15 dígitos, correo opcional con dominio completo, horario opcional de 3–300 caracteres, coordenadas finitas dentro de rango y hasta 7 decimales, enlace HTTPS específico de Google Maps. Errores visibles por campo y accesibles; guardar se bloquea cuando el formulario es inválido o aún no cargó. Los errores por campo del backend también se muestran en el formulario.

## Tabla de usuarios

`/administracion/usuarios` presenta el directorio real de usuarios con contadores, foto o iniciales, nombre/correo, contacto, rol, estado y fecha de registro. `UsuariosService` utiliza GET `/api/usuarios` y PATCH `/api/usuarios/:id/toggle-activo`. Incluye búsqueda local con debounce de 300 ms, filtros por rol/estado, orden por fecha/nombre, paginación 10/20/50, estados vacío/error/carga y recarga.

El detalle se abre en un diálogo nativo. Cambiar estado requiere confirmación; la interfaz bloquea desactivar la cuenta propia. No se agregaron formularios de creación/edición ni acciones de borrado. Las rutas y endpoints mantienen el acceso exclusivo ADMIN. Los cambios de estado de las pruebas se realizan con servicios simulados.

Pruebas: `npx ng test --watch=false --include=src/app/pages/administracion/usuarios/usuarios.spec.ts --ts-config=tsconfig.usuarios-check.json`.

### Cambiar rol

Desde el detalle de usuario se puede seleccionar Cliente, Empleado o Administrador y confirmar el cambio. La acción usa PATCH `/api/usuarios/:id/rol` con solo `{rol}`. Se actualizan fila, contadores y filtros con la respuesta del backend. El panel y el endpoint impiden cambiar el propio rol. Los cambios de permisos de otra cuenta se reflejan en la autorización del backend en sus siguientes peticiones; la interfaz de esa cuenta sincroniza su rol al cargar la sesión.
