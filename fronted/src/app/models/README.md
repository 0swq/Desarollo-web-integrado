# Modelos del frontend

Una interfaz por cada clase de backend/model y tipos de unión para EstadoOrden, EstadoPago y TipoMovimiento. Rol conserva sus constantes y tipo compartidos.

UUID y LocalDateTime se representan como string; BigDecimal e Integer como number; Boolean como boolean. Los campos que permiten nulos se expresan con null y son opcionales para admitir respuestas resumidas. Las relaciones son referencias a otras interfaces y pueden omitirse cuando la API devuelve solo sus identificadores. Usuario.contrasena es opcional y solo para entrada.

Las interfaces describen modelos; los endpoints que devuelven DTO pueden tener otra forma. CarritoResponse, ItemCarritoResponse y OrdenRequest de orden.interface.ts conservan su contrato previo de pantalla; el modelo ItemCarrito está en item-carrito.interface.ts y no incluye el subtotal calculado del DTO.
