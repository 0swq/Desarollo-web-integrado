# Imágenes de los modelos de vehículos

Se asignó una fotografía a cada uno de los 49 modelos registrados. Los originales se conservan en `originales/` y la correspondencia modelo/fuente está en `seleccion.json`.

Las imágenes se cargaron con `SupabaseStorageService` en la carpeta `modelos` del bucket privado `autopartes-imagenes`. Cada registro de `modelo_vehiculo.imagen_url` guarda una URL de streaming del backend, no un enlace externo ni una URL firmada temporal. `registros-*.json` documenta UUID, marca, modelo, ruta del bucket, URL actual y URL anterior.

Son imágenes referenciales de la familia del modelo; no certifican la versión, año, motorización ni compatibilidad de repuestos. No se modificaron nombres, marcas, tipos de vehículos, productos ni compatibilidades.

## Fuentes

Las fotografías se tomaron de las páginas públicas oficiales de Toyota, Nissan, Hyundai, Kia, Chevrolet, Suzuki, Mitsubishi, Honda y Mazda. Para Volkswagen se utilizaron las fotografías de prensa públicas de Volkswagen Newsroom para Polo, T-Cross y Tiguan. La página y URL exactas de origen de cada imagen están en `seleccion.json`.

Los derechos de las fotografías de fabricante permanecen en sus respectivos titulares. Las imágenes de Volkswagen Newsroom conservan su identificación de origen para usos de prensa/editoriales según su fuente.

### Atribución Volkswagen Virtus

- Imagen: **2022 Volkswagen Virtus 1.5 GT (India), front view 02**.
- Autor: **DriveSpark**.
- Fuente: [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:2022_Volkswagen_Virtus_1.5_GT_(India)_front_view_02.png).
- Licencia: [Creative Commons Attribution 3.0](https://creativecommons.org/licenses/by/3.0/).
- Imagen sin modificaciones. Los metadatos de atribución originales se conservan en `virtus-licencia.json` y en `seleccion.json`.

## Carga

`CargarImagenesVehiculos.java` lee las credenciales de `backend/.env`, sube las imágenes mediante el servicio del backend y actualiza exclusivamente `modelo_vehiculo.imagen_url` en una transacción. Comprueba la coincidencia exacta de marca y modelo antes de subir; si detecta cambios concurrentes, revierte la transacción y elimina solo las nuevas cargas de esa ejecución. No imprime credenciales.
