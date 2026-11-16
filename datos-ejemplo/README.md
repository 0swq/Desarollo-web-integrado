# Datos de ejemplo del catálogo

Carga solicitada el 14 de noviembre de 2026. Incluye categorías de repuestos, marcas y modelos; no contiene productos, proveedores, existencias ni compatibilidades.

- `categorias.csv`: categorías generales de mantenimiento y repuestos; son una clasificación editorial.
- `vehiculos.csv`: marca, país de origen de la marca, nombre del modelo y tipo de carrocería. El país no representa el lugar de ensamblaje de cada vehículo.
- `SeedCatalogo.java`: carga transaccional e idempotente. Reutiliza marcas, categorías y modelos existentes por nombre; no borra ni actualiza registros existentes. Lee la contraseña del archivo `backend/.env` y nunca la imprime. Comprueba que el conteo de productos no cambie.

Los nombres se normalizaron para eliminar prefijos publicitarios como “All New”. Las familias de modelos no representan generaciones, años, motores ni compatibilidades de piezas. El esquema actual almacena años y motores en compatibilidades relacionadas con productos; esos registros se dejan pendientes.

## Fuentes de modelos

Consultadas el 14 de noviembre de 2026:

- Toyota: https://e.toyotaperu.com.pe/inventory y https://www.kinto-mobility.com.pe/kinto-one/modelos
- Nissan: https://www.nissan.pe/vehiculos/nuevos.html
- Hyundai: https://hyundai.pe/modelos/
- Kia: https://www.kia.com/pe/showroom/autos-nuevos.html
- Chevrolet: https://www.chevrolet.com.pe/ y https://www.chevrolet.com.pe/camionetas
- Suzuki: https://www.suzuki.com.pe/blog/modelos-de-carros-suzuki-en-peru/
- Mitsubishi: https://www.mitsubishi-motors.com.pe/fichas-tecnicas y https://www.mitsubishi-motors.com.pe/doc/specs-new-montero-sport.pdf
- Honda: https://autos.honda.com.pe/
- Mazda: https://www.mazda.pe/
- Volkswagen: https://www.volkswagen.com.pe/es/modelos.html

## Conservación de datos

Iniciar Spring Boot con `spring.jpa.hibernate.ddl-auto=create` elimina y recrea las tablas. Para conservar esta carga, utilizar `update` o `validate` en los siguientes arranques y retirar cualquier argumento que fuerce `create`.

## Productos agregados posteriormente

A petición del usuario se cargaron cinco productos mediante SeedProductos.java, cada uno con categoría y stock. Los SKU DEMO-* identifican datos sintéticos, con precios e inventario de prueba; no se asignaron equivalencias OEM ni compatibilidades de vehículos. Dos tienen precio promocional y uno está agotado para probar esos filtros. La carga es transaccional e ignora SKU existentes. Ejecutar el archivo fuente Java con la opción '-Dfile.encoding=UTF-8' para conservar las tildes.


## Compatibilidades de demostración

Se cargaron 20 asociaciones para los cinco SKU DEMO, cuatro por producto, usando los modelos ya registrados. compatibilidades-demo.csv contiene las filas y SeedCompatibilidades.java realiza la carga transaccional e idempotente. Todos los motores llevan la etiqueta (DEMO) y cada registro incluye en notas que la asociación es ficticia para probar filtros y la ficha. Los rangos de años y motores no acreditan compatibilidad técnica real ni equivalencia OEM.
