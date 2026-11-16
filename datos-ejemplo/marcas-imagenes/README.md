# Imágenes de marcas

Los diez logotipos se conservaron en originales/ y se subieron a la carpeta marcas del bucket privado autopartes-imagenes. Cada marca guarda la URL de streaming del backend en imagenUrl. La creación y los PUT de marcas admiten este campo; omitirlo conserva el anterior y una cadena vacía lo elimina.

Los originales son PNG servidos por Wikimedia Commons. seleccion.json conserva la fuente y los metadatos de atribución originales. Los símbolos identifican a sus respectivos titulares.

| Marca | Fuente | Licencia declarada | Autor declarado |
|---|---|---|---|
| Toyota | [Fuente](https://commons.wikimedia.org/wiki/File:Toyota_carlogo.svg) | Public domain | Toyota Motor Corporation |
| Suzuki | [Fuente](https://commons.wikimedia.org/wiki/File:Suzuki_logo_2025_(vertical).svg) | Public domain | Suzuki Motor Corporation |
| Volkswagen | [Fuente](https://commons.wikimedia.org/wiki/File:Volkswagen_logo_2019.svg) | Public domain | Volkswagen |
| Chevrolet | [Fuente](https://commons.wikimedia.org/wiki/File:Chevrolet-logo.png) | Public domain | Diego HC |
| Mitsubishi | [Fuente](https://commons.wikimedia.org/wiki/File:Mitsubishi_motors_new_logo.svg) | Public domain | Mitsubishi Motors Corporation |
| Nissan | [Fuente](https://commons.wikimedia.org/wiki/File:Nissan_2020_logo.svg) | Public domain | Nissan Motor Corporation |
| Kia | [Fuente](https://commons.wikimedia.org/wiki/File:KIA_logo3.svg) | Public domain | Kia Corporation |
| Mazda | [Fuente](https://commons.wikimedia.org/wiki/File:Mazda_logo.svg) | Public domain | TTTNIS |
| Hyundai | [Fuente](https://commons.wikimedia.org/wiki/File:Hyundai_Motor_Company_logo.svg) | Public domain | Hyundai Motor Company |
| Honda | [Fuente](https://commons.wikimedia.org/wiki/File:Honda-logo.svg) | Public domain | Honda Motor Company |

Los archivos registros-*.json contienen la relación entre UUID, marca y URL del bucket. verificacion.json registra la comprobación de las diez imágenes por streaming. CargarImagenesMarcas.java realiza la carga y actualización en una transacción, sin modificar los modelos ni productos.
