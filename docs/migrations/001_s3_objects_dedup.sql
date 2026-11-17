-- ============================================================================
-- Migración: deduplicación por contenido (tabla s3_objects)
-- ----------------------------------------------------------------------------
-- Ejecutar UNA VEZ contra la base de datos `amazon`, DESPUÉS de que la app haya
-- arrancado una primera vez con la nueva versión (Hibernate ddl-auto=update crea
-- la tabla s3_objects y las columnas s3_object_id/content_hash/size_bytes en
-- amazon_s3_files automáticamente).
--
-- Qué hace:
--   1. Elimina el índice único uk_s3_files_key_bucket si existiera (con dedup,
--      varias referencias lógicas pueden compartir la misma key).
--   2. Registra en s3_objects los objetos físicos ya existentes (uno por fila
--      histórica; no se puede deduplicar retroactivamente sin el contenido, así
--      que cada fila legacy obtiene su propio objeto con hash marcador).
--   3. Enlaza cada fila de amazon_s3_files con su s3_object.
-- ============================================================================

-- 1) Quitar el índice único anterior (si existe). La unicidad (key, bucket_aws_id)
--    ya no aplica: el dedup permite varias referencias sobre la misma clave.
SET @idx := (
    SELECT INDEX_NAME
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'amazon_s3_files'
      AND INDEX_NAME = 'uk_s3_files_key_bucket'
    LIMIT 1
);
SET @sql := IF(@idx IS NULL,
               'SELECT "uk_s3_files_key_bucket no existe"',
               'ALTER TABLE amazon_s3_files DROP INDEX uk_s3_files_key_bucket');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Backfill de s3_objects desde filas históricas sin enlace.
--    content_hash recibe un marcador 'legacy-<id>' único: evita deduplicar
--    incorrectamente contenido desconocido y permite que nuevas subidas con el
--    mismo contenido real creen su objeto SHA-256 normal.
INSERT INTO s3_objects (bucket_aws_id, object_key, content_hash, e_tag, s3_url,
                        usr_crea, usr_mod, created_at, updated_at, deleted)
SELECT f.bucket_aws_id,
       f.`key`,
       CONCAT('legacy-', f.id_files_amazon_s3),
       f.e_tag,
       f.s3_url,
       COALESCE(f.usr_crea, 1),
       f.usr_mod,
       f.created_at,
       f.updated_at,
       COALESCE(f.deleted, 0)
FROM amazon_s3_files f
WHERE f.s3_object_id IS NULL
  AND f.`key` IS NOT NULL;

-- 3) Enlazar las referencias con su objeto recién creado (por key + bucket).
UPDATE amazon_s3_files f
JOIN s3_objects o
  ON o.bucket_aws_id = f.bucket_aws_id
 AND o.object_key = f.`key`
 AND o.content_hash = CONCAT('legacy-', f.id_files_amazon_s3)
SET f.s3_object_id = o.id_s3_object,
    f.content_hash = o.content_hash
WHERE f.s3_object_id IS NULL;

-- Verificación: no debe quedar ninguna fila con key pero sin s3_object_id.
SELECT COUNT(*) AS pendientes
FROM amazon_s3_files
WHERE `key` IS NOT NULL AND s3_object_id IS NULL;
