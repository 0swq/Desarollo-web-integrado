-- ==========================================================
-- V1__init_schema.sql
-- Sistema de Ventas de Autopartes
-- ==========================================================

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Categorías
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    imagen_url VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Proveedores
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    contacto_nombre VARCHAR(100),
    telefono VARCHAR(20),
    email VARCHAR(120),
    direccion VARCHAR(250),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Productos / Autopartes
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000),
    precio NUMERIC(10, 2) NOT NULL,
    precio_promocional NUMERIC(10, 2),
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    imagen_url VARCHAR(255),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    supplier_id BIGINT REFERENCES suppliers(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Módulo Vehicular: Marcas
CREATE TABLE IF NOT EXISTS vehicle_makes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    pais_origen VARCHAR(60),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Módulo Vehicular: Modelos
CREATE TABLE IF NOT EXISTS vehicle_models (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    make_id BIGINT NOT NULL REFERENCES vehicle_makes(id),
    tipo_vehiculo VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Módulo Vehicular: Compatibilidad Autoparte - Modelo - Rango Años
CREATE TABLE IF NOT EXISTS vehicle_compatibilities (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    vehicle_model_id BIGINT NOT NULL REFERENCES vehicle_models(id),
    anio_inicio INTEGER NOT NULL,
    anio_fin INTEGER NOT NULL,
    motor VARCHAR(80),
    notas VARCHAR(250)
);

-- Tabla de Stock (Inventario físico)
CREATE TABLE IF NOT EXISTS stocks (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE REFERENCES products(id),
    cantidad INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 5,
    ubicacion_almacen VARCHAR(60),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Movimientos Kardex
CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    tipo VARCHAR(20) NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_nuevo INTEGER NOT NULL,
    motivo VARCHAR(250) NOT NULL,
    referencia VARCHAR(60),
    user_id BIGINT REFERENCES users(id),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Carrito de Compras
CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(10, 2) NOT NULL
);

-- Órdenes y Ventas
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    numero_orden VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    estado VARCHAR(25) NOT NULL DEFAULT 'PENDIENTE',
    subtotal NUMERIC(10, 2) NOT NULL,
    igv NUMERIC(10, 2) NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    direccion_entrega VARCHAR(250) NOT NULL,
    telefono_contacto VARCHAR(20),
    notas VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    nombre_producto VARCHAR(150) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL
);

-- Pagos (Mercado Pago metadata)
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    monto NUMERIC(10, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    metodo_pago VARCHAR(50),
    mercado_pago_payment_id VARCHAR(100),
    mercado_pago_preference_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Datos semilla iniciales para pruebas
INSERT INTO vehicle_makes (nombre, pais_origen, activo) VALUES
('Toyota', 'Japón', true),
('Nissan', 'Japón', true),
('Hyundai', 'Corea del Sur', true),
('Kia', 'Corea del Sur', true),
('Chevrolet', 'EE.UU.', true)
ON CONFLICT (nombre) DO NOTHING;
