# Diagrama de Entidades JPA - Autopartes Backend

> Generado automáticamente desde las clases de modelo (`com.autopartes.model`)

---

## 📋 Índice de Tablas

| # | Entidad | Tabla BD | Descripción |
|---|---------|----------|-------------|
| 1 | `User` | `users` | Usuarios del sistema (clientes, empleados, admins) |
| 2 | `Category` | `categories` | Categorías de productos/autopartes |
| 3 | `Supplier` | `suppliers` | Proveedores con RUC |
| 4 | `Product` | `products` | Autopartes (SKU, precios, stock) |
| 5 | `VehicleMake` | `vehicle_makes` | Marcas de vehículos (Toyota, Nissan, etc.) |
| 6 | `VehicleModel` | `vehicle_models` | Modelos de vehículos por marca |
| 7 | `VehicleCompatibility` | `vehicle_compatibilities` | Compatibilidad producto ↔ modelo + rango años |
| 8 | `Stock` | `stocks` | Inventario físico 1:1 con producto |
| 9 | `StockMovement` | `stock_movements` | Kardex: movimientos de entrada/salida/ajuste |
| 10 | `Cart` | `carts` | Carrito de compras por usuario |
| 11 | `CartItem` | `cart_items` | Ítems del carrito |
| 12 | `Order` | `orders` | Órdenes de venta con IGV (18%) |
| 13 | `OrderItem` | `order_items` | Detalle de productos en la orden |
| 14 | `Payment` | `payments` | Pagos (integración Mercado Pago) |
| 15 | `Role` (enum) | — | Roles: ADMIN, EMPLEADO, CLIENTE |
| 16 | `OrderStatus` (enum) | — | Estados: PENDIENTE, PAGADO, EN_PREPARACION, ENVIADO, ENTREGADO, CANCELADO |
| 17 | `PaymentStatus` (enum) | — | Estados: PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO |
| 18 | `StockMovementType` (enum) | — | Tipos: ENTRADA, SALIDA, AJUSTE |

---

## 1. User → `users`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `email` | `String` | `VARCHAR(120)` | `@Column(nullable=false, unique=true, length=120)` |
| `password` | `String` | `VARCHAR(255)` | `@Column(nullable=false)` (BCrypt hash) |
| `nombre` | `String` | `VARCHAR(80)` | `@Column(nullable=false, length=80)` |
| `apellido` | `String` | `VARCHAR(80)` | `@Column(nullable=false, length=80)` |
| `telefono` | `String` | `VARCHAR(20)` | `@Column(length=20)` |
| `direccion` | `String` | `VARCHAR(200)` | `@Column(length=200)` |
| `rol` | `Role` (enum) | `VARCHAR(20)` | `@Enumerated(STRING)`, `@Column(nullable=false, length=20)` |
| `activo` | `Boolean` | `BOOLEAN` | `@Column(nullable=false)`, `@Builder.Default = true` |
| `createdAt` | `LocalDateTime` | `TIMESTAMP` | `@CreatedDate`, `@Column(nullable=false, updatable=false)` |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP` | `@LastModifiedDate`, `@Column(nullable=false)` |

**Relaciones:** Ninguna directa (unidireccional desde otras entidades)

---

## 2. Category → `categories`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `String` | `VARCHAR(100)` | `@Column(nullable=false, unique=true, length=100)` |
| `descripcion` | `String` | `VARCHAR(500)` | `@Column(length=500)` |
| `imagenUrl` | `String` | `VARCHAR(255)` | `@Column(length=255)` |
| `activo` | `Boolean` | `BOOLEAN` | `@Column(nullable=false)`, `@Builder.Default = true` |
| `createdAt` | `LocalDateTime` | `TIMESTAMP` | `@CreatedDate`, `@Column(nullable=false, updatable=false)` |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP` | `@LastModifiedDate`, `@Column(nullable=false)` |

**Relaciones:** `Product.categoria` → `@ManyToOne` (FK `category_id`)

---

## 3. Supplier → `suppliers`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `ruc` | `String` | `VARCHAR(11)` | `@Column(nullable=false, unique=true, length=11)` |
| `razonSocial` | `String` | `VARCHAR(150)` | `@Column(nullable=false, length=150)` |
| `contactoNombre` | `String` | `VARCHAR(100)` | `@Column(length=100)` |
| `telefono` | `String` | `VARCHAR(20)` | `@Column(length=20)` |
| `email` | `String` | `VARCHAR(120)` | `@Column(length=120)` |
| `direccion` | `String` | `VARCHAR(250)` | `@Column(length=250)` |
| `activo` | `Boolean` | `BOOLEAN` | `@Column(nullable=false)`, `@Builder.Default = true` |
| `createdAt` | `LocalDateTime` | `TIMESTAMP` | `@CreatedDate`, `@Column(nullable=false, updatable=false)` |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP` | `@LastModifiedDate`, `@Column(nullable=false)` |

**Relaciones:** `Product.proveedor` → `@ManyToOne` (FK `supplier_id`, nullable)

---

## 4. Product → `products`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `sku` | `String` | `VARCHAR(50)` | `@Column(nullable=false, unique=true, length=50)` |
| `nombre` | `String` | `VARCHAR(150)` | `@Column(nullable=false, length=150)` |
| `descripcion` | `String` | `VARCHAR(1000)` | `@Column(length=1000)` |
| `precio` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` |
| `precioPromocional` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(precision=10, scale=2)` (nullable) |
| `moneda` | `String` | `VARCHAR(3)` | `@Column(nullable=false, length=3)`, `@Builder.Default = "PEN"` |
| `imagenUrl` | `String` | `VARCHAR(255)` | `@Column(length=255)` |
| `categoria` | `Category` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="category_id", nullable=false)` |
| `proveedor` | `Supplier` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="supplier_id")` (nullable) |
| `activo` | `Boolean` | `BOOLEAN` | `@Column(nullable=false)`, `@Builder.Default = true` |
| `createdAt` | `LocalDateTime` | `TIMESTAMP` | `@CreatedDate`, `@Column(nullable=false, updatable=false)` |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP` | `@LastModifiedDate`, `@Column(nullable=false)` |

**Relaciones inversas:**
- `VehicleCompatibility.product` → `@ManyToOne` (FK `product_id`)
- `Stock.product` → `@OneToOne` (FK `product_id`, unique)
- `StockMovement.product` → `@ManyToOne` (FK `product_id`)
- `CartItem.product` → `@ManyToOne` (FK `product_id`)
- `OrderItem.product` → `@ManyToOne` (FK `product_id`)

---

## 5. VehicleMake → `vehicle_makes`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `String` | `VARCHAR(80)` | `@Column(nullable=false, unique=true, length=80)` |
| `paisOrigen` | `String` | `VARCHAR(60)` | `@Column(length=60)` |
| `activo` | `Boolean` | `BOOLEAN` | `@Column(nullable=false)`, `@Builder.Default = true` |

**Relaciones:** `VehicleModel.make` → `@ManyToOne` (FK `make_id`)

---

## 6. VehicleModel → `vehicle_models`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `nombre` | `String` | `VARCHAR(100)` | `@Column(nullable=false, length=100)` |
| `make` | `VehicleMake` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="make_id", nullable=false)` |
| `tipoVehiculo` | `String` | `VARCHAR(50)` | `@Column(length=50)` (ej: Sedan, SUV, Camioneta, Hatchback) |
| `activo` | `Boolean` | `BOOLEAN` | `@Column(nullable=false)`, `@Builder.Default = true` |

**Relaciones inversas:** `VehicleCompatibility.vehicleModel` → `@ManyToOne` (FK `vehicle_model_id`)

---

## 7. VehicleCompatibility → `vehicle_compatibilities`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `product` | `Product` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="product_id", nullable=false)` |
| `vehicleModel` | `VehicleModel` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="vehicle_model_id", nullable=false)` |
| `anioInicio` | `Integer` | `INTEGER` | `@Column(nullable=false)` |
| `anioFin` | `Integer` | `INTEGER` | `@Column(nullable=false)` |
| `motor` | `String` | `VARCHAR(80)` | `@Column(length=80)` (ej: "1.5L Dual VVT-i", "2.4L Diesel") |
| `notas` | `String` | `VARCHAR(250)` | `@Column(length=250)` (ej: "Compatible solo con versión manual") |

**Índice compuesto sugerido:** `(product_id, vehicle_model_id, anio_inicio, anio_fin)`

---

## 8. Stock → `stocks`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `product` | `Product` | `BIGINT FK UNIQUE` | `@OneToOne(LAZY)`, `@JoinColumn(name="product_id", nullable=false, unique=true)` |
| `cantidad` | `Integer` | `INTEGER` | `@Column(nullable=false)`, `@Builder.Default = 0` |
| `stockMinimo` | `Integer` | `INTEGER` | `@Column(nullable=false)`, `@Builder.Default = 5` |
| `ubicacionAlmacen` | `String` | `VARCHAR(60)` | `@Column(length=60)` (ej: "Pasillo B - Estante 4") |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP` | `@LastModifiedDate`, `@Column(nullable=false)` |

**Método helper:** `isBajoStock()` → `cantidad <= stockMinimo`

---

## 9. StockMovement → `stock_movements` (Kardex)

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `product` | `Product` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="product_id", nullable=false)` |
| `tipo` | `StockMovementType` (enum) | `VARCHAR(20)` | `@Enumerated(STRING)`, `@Column(nullable=false, length=20)` |
| `cantidad` | `Integer` | `INTEGER` | `@Column(nullable=false)` |
| `stockAnterior` | `Integer` | `INTEGER` | `@Column(nullable=false)` |
| `stockNuevo` | `Integer` | `INTEGER` | `@Column(nullable=false)` |
| `motivo` | `String` | `VARCHAR(250)` | `@Column(nullable=false, length=250)` (ej: "Compra a proveedor RUC 20...", "Venta Orden #1024") |
| `referencia` | `String` | `VARCHAR(60)` | `@Column(length=60)` (código orden, factura, guía) |
| `usuario` | `User` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="user_id")` (nullable) |
| `fecha` | `LocalDateTime` | `TIMESTAMP` | `@CreatedDate`, `@Column(nullable=false, updatable=false)` |

**Valores enum `StockMovementType`:** `ENTRADA`, `SALIDA`, `AJUSTE`

---

## 10. Cart → `carts`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `user` | `User` | `BIGINT FK UNIQUE` | `@OneToOne(LAZY)`, `@JoinColumn(name="user_id", nullable=false, unique=true)` |
| `items` | `List<CartItem>` | — | `@OneToMany(mappedBy="cart", cascade=ALL, orphanRemoval=true)` |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP` | `@LastModifiedDate`, `@Column(nullable=false)` |

---

## 11. CartItem → `cart_items`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `cart` | `Cart` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="cart_id", nullable=false)` |
| `product` | `Product` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="product_id", nullable=false)` |
| `cantidad` | `Integer` | `INTEGER` | `@Column(nullable=false)` |
| `precioUnitario` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` |

**Método helper:** `getSubtotal()` → `precioUnitario * cantidad`

---

## 12. Order → `orders`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `numeroOrden` | `String` | `VARCHAR(30)` | `@Column(nullable=false, unique=true, length=30)` (ej: "ORD-20260904-0001") |
| `cliente` | `User` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="user_id", nullable=false)` |
| `estado` | `OrderStatus` (enum) | `VARCHAR(25)` | `@Enumerated(STRING)`, `@Column(nullable=false, length=25)`, `@Builder.Default = PENDIENTE` |
| `subtotal` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` |
| `igv` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` (18% estándar Perú) |
| `total` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` |
| `moneda` | `String` | `VARCHAR(3)` | `@Column(nullable=false, length=3)`, `@Builder.Default = "PEN"` |
| `direccionEntrega` | `String` | `VARCHAR(250)` | `@Column(nullable=false, length=250)` |
| `telefonoContacto` | `String` | `VARCHAR(20)` | `@Column(length=20)` |
| `notas` | `String` | `VARCHAR(500)` | `@Column(length=500)` |
| `items` | `List<OrderItem>` | — | `@OneToMany(mappedBy="order", cascade=ALL, orphanRemoval=true)` |
| `createdAt` | `LocalDateTime` | `TIMESTAMP` | `@CreatedDate`, `@Column(nullable=false, updatable=false)` |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP` | `@LastModifiedDate`, `@Column(nullable=false)` |

**Valores enum `OrderStatus`:** `PENDIENTE`, `PAGADO`, `EN_PREPARACION`, `ENVIADO`, `ENTREGADO`, `CANCELADO`

---

## 13. OrderItem → `order_items`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `order` | `Order` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="order_id", nullable=false)` |
| `product` | `Product` | `BIGINT FK` | `@ManyToOne(LAZY)`, `@JoinColumn(name="product_id", nullable=false)` |
| `nombreProducto` | `String` | `VARCHAR(150)` | `@Column(nullable=false, length=150)` (copia histórica) |
| `sku` | `String` | `VARCHAR(50)` | `@Column(nullable=false, length=50)` (copia histórica) |
| `cantidad` | `Integer` | `INTEGER` | `@Column(nullable=false)` |
| `precioUnitario` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` |
| `subtotal` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` |

---

## 14. Payment → `payments`

| Atributo | Tipo Java | Tipo BD | Anotaciones / Constraints |
|----------|-----------|---------|---------------------------|
| `id` | `Long` | `BIGSERIAL PK` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `order` | `Order` | `BIGINT FK UNIQUE` | `@OneToOne(LAZY)`, `@JoinColumn(name="order_id", nullable=false, unique=true)` |
| `monto` | `BigDecimal` | `NUMERIC(10,2)` | `@Column(nullable=false, precision=10, scale=2)` |
| `moneda` | `String` | `VARCHAR(3)` | `@Column(nullable=false, length=3)`, `@Builder.Default = "PEN"` |
| `status` | `PaymentStatus` (enum) | `VARCHAR(20)` | `@Enumerated(STRING)`, `@Column(nullable=false, length=20)`, `@Builder.Default = PENDIENTE` |
| `metodoPago` | `String` | `VARCHAR(50)` | `@Column(length=50)` (ej: "MERCADO_PAGO_TARJETA", "MERCADO_PAGO_YAPE", "EFECTIVO") |
| `mercadoPagoPaymentId` | `String` | `VARCHAR(100)` | `@Column(length=100)` |
| `mercadoPagoPreferenceId` | `String` | `VARCHAR(100)` | `@Column(length=100)` |
| `createdAt` | `LocalDateTime` | `TIMESTAMP` | `@CreatedDate`, `@Column(nullable=false, updatable=false)` |

**Valores enum `PaymentStatus`:** `PENDIENTE`, `APROBADO`, `RECHAZADO`, `REEMBOLSADO`

---

## 15-18. Enums (sin tabla propia)

### `Role` (usado en `User.rol`)
```java
ROLE_ADMIN, ROLE_EMPLEADO, ROLE_CLIENTE
```

### `OrderStatus` (usado en `Order.estado`)
```java
PENDIENTE, PAGADO, EN_PREPARACION, ENVIADO, ENTREGADO, CANCELADO
```

### `PaymentStatus` (usado en `Payment.status`)
```java
PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO
```

### `StockMovementType` (usado en `StockMovement.tipo`)
```java
ENTRADA, SALIDA, AJUSTE
```

---

## 🔗 Resumen de Relaciones

```
User (1) ────── (1) Cart (1) ────── (N) CartItem (N) ────── (1) Product
    │                                                    │
    │                                                    │
    └─ (N) Order ── (N) OrderItem ── (1) Product         │
         │                                              │
         └─ (1) Payment                                 │
                                                          │
VehicleMake (1) ── (N) VehicleModel (1) ── (N) VehicleCompatibility (N) ── (1) Product
                                                          │
Supplier (1) ────────────────────────────────────────────┘
                                                          │
Stock (1) ────────────────────────────────────────────────┘
    │
    └─ (N) StockMovement ── (N:1) User
```

---

## 📝 Notas de Implementación

- **Auditoría:** `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`/`@LastModifiedDate` en la mayoría
- **Soft delete:** Campo `activo` (Boolean) en User, Category, Supplier, Product, VehicleMake, VehicleModel
- **Moneda:** Por defecto `PEN` (Soles peruanos), IGV 18% hardcoded en lógica de negocio
- **Historial:** `OrderItem` guarda `nombreProducto` y `sku` como copia histórica
- **Kardex:** `StockMovement` registra `stockAnterior` y `stockNuevo` para trazabilidad completa
- **Lazy loading:** Todas las relaciones `@ManyToOne` y `@OneToOne` usan `FetchType.LAZY`