# Compra y pasarela simulada

Endpoints autenticados, siempre limitados al propietario del pedido:

- POST `/api/checkout/ordenes`: dirección de entrega (hasta 255), teléfono (hasta 20), notas (hasta 500). El servidor fija el método SIMULADO y el usuario autenticado. Devuelve orden, pago y líneas; no permite elegir un usuario o un método real.
- GET `/api/checkout/ordenes/{id}`: resumen para recargar o consultar el resultado.
- POST `/api/checkout/ordenes/{id}/simular`: `{ "resultado": "APROBADO" }` o RECHAZADO. Solo admite pagos de método SIMULADO de órdenes propias pendientes. Aprobar marca pago APROBADO y orden PAGADO; rechazar deja orden PENDIENTE. Repetir una aprobación no vuelve a modificar el pedido.

La creación usa la transacción de OrdenService y bloquea carrito y productos. Un fallo revierte stock, movimientos, pedido y pago. Las llamadas repetidas sobre el mismo carrito no crean otra orden una vez vaciado. Las líneas se bloquean en orden de UUID para evitar bloqueos cruzados entre compras. No se modificaron los endpoints administrativos de pagos.

Los pedidos simulados llevan número SIM-. No hay integración bancaria ni cobros. El inventario sí cambia y el carrito se vacía cuando se crea el pedido. Los pedidos rechazados o pendientes conservan el stock descontado hasta que la tienda los cancele mediante el flujo administrativo existente. El usuario puede reintentar el pago del pedido creado.

Pruebas: `mvn -Dtest=CheckoutControllerTest test`. No ejecutan cobros ni crean pedidos contra la base real.
