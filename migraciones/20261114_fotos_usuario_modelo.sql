-- Columnas opcionales. No modifica ni elimina registros existentes.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS foto_perfil_url varchar(2048);
ALTER TABLE modelo_vehiculo ADD COLUMN IF NOT EXISTS imagen_url varchar(2048);
