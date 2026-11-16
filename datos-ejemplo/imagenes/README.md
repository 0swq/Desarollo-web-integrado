# Imágenes generadas de los productos de demostración

Se generaron cinco imágenes mediante la herramienta integrada de generación de imágenes y se conservaron aquí los originales PNG. Todas son ilustraciones de productos genéricos de demostración; no representan referencias OEM ni marcas comerciales reales.

| SKU | Archivo original |
|---|---|
| DEMO-ACE-5W30-1L | DEMO-ACE-5W30-1L.png |
| DEMO-REF-5050-1L | DEMO-REF-5050-1L.png |
| DEMO-LUZ-H4-12V | DEMO-LUZ-H4-12V.png |
| DEMO-PLU-20IN | DEMO-PLU-20IN.png |
| DEMO-FIL-ACE-001 | DEMO-FIL-ACE-001.png |

Los prompts completos utilizados se conservan en `prompts.json`. El estilo común es fotografía de producto de estudio, encuadre cuadrado, fondo claro, sombra suave y ausencia de marcas reales.

Las imágenes se cargaron en el bucket privado `autopartes-imagenes` mediante `SupabaseStorageService.crear`. Las URLs de streaming devueltas por ese servicio se guardaron en `producto.imagen_url`. El archivo `registros-*.json` contiene la correspondencia exacta entre SKU, objeto del bucket y URL; no contiene credenciales.

La carga y vinculación se realizó con `../CargarImagenesProductos.java`, que conserva precios, categorías y existencias. Ante un fallo antes del commit, revierte las actualizaciones y elimina únicamente los archivos nuevos de esa ejecución.
