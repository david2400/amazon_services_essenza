# Imágenes de correo en S3 (`email/imagenes/`)

El servicio **notificacion** sube las imágenes de su biblioteca de correo a través de este servicio y
pega la `s3Url` devuelta en correos HTML. Gmail y Outlook descargan esa URL cada vez que alguien abre
el correo, incluso meses después. Por eso:

- los objetos bajo `email/imagenes/` deben ser **públicos de lectura**, y el resto del bucket privado;
- las claves son `<carpeta>/<uuid>.<ext>` y **nunca se sobrescriben**, así que se cachean un año;
- la URL es **permanente** (no una URL prefirmada).

> Ningún paso de esta guía lo ejecuta la aplicación. Los cambios en AWS los aplica una persona con su
> propio perfil de AWS CLI.

## Contrato que usa notificacion (no cambiar)

Las rutas reales llevan el prefijo `/api/aws` (`ApiPrefixConfiguration`).

| Operación | Ruta | Parámetros | Respuesta |
|---|---|---|---|
| Subir | `POST /api/aws/files-amazon/subir_archivo` (multipart) | `file`, `bucket`, `carpeta` (= `email/imagenes/aaaa/mm`), `bucket_aws_id` | JSON con `id`, `key`, `s3Url` (y el resto de `FilesAmazonS3Dto`) |
| Borrar | `DELETE /api/aws/files-amazon/eliminar_archivo?clave=<key>&bucketAwsId=<id>` | — | 200 con la lista del archivo borrado; **404** si no existe |

Respuestas de error (JSON `{timestamp, status, error, message}`):

| Caso | Código |
|---|---|
| `carpeta` con `..`, `\`, `//` o caracteres de control | 400 |
| `bucket` distinto del `bucketName` registrado para `bucket_aws_id`, o id no registrado | 400 |
| Falta un parámetro obligatorio | 400 |
| Archivo mayor que `spring.servlet.multipart.max-file-size` (10 MB) | 413 |
| Sin token o token inválido (con `AWS_SERVICES_ENFORCE_AUTH=true`) | 401 |
| `clave` + `bucketAwsId` inexistente (o ya borrada) | 404 |
| Fallo de S3 | 502 (mensaje genérico, sin trazas) |

Al borrar, el objeto se elimina de S3 y la fila de `amazon_s3_files` queda con soft delete (`deleted = 1`);
un segundo borrado de la misma clave responde 404.

## Cabeceras con las que se guardan los objetos

Lo hace `AwsS3StorageGateway.upload` para **toda** subida:

- `Content-Type`: el del multipart. Si viene vacío o `application/octet-stream`, se deduce de la extensión
  (`png` → `image/png`, `jpg` → `image/jpeg`, `gif` → `image/gif`, ...).

Solo para claves bajo un prefijo de `aws.s3.prefijos-publicos` (por defecto `email/imagenes/`):

- `Cache-Control: public, max-age=31536000, immutable`
- `Content-Disposition: inline`

El resto de archivos se sube exactamente igual que antes.

## URL devuelta (`s3Url`)

Formato regional y siempre `https`:

```
https://<bucket>.s3.<region>.amazonaws.com/<key>
```

- `<region>` sale de `aws.region` (`AWS_REGION`).
- Cada segmento de la clave se codifica (RFC 3986) y se conservan los `/`.
- Si el nombre del bucket tiene puntos se usa path-style (`https://s3.<region>.amazonaws.com/<bucket>/<key>`),
  porque el certificado TLS de S3 no cubre subdominios con puntos.

Antes se usaba `s3Client.utilities().getUrl`, que con `us-east-1` devuelve el endpoint global
`https://<bucket>.s3.amazonaws.com/<key>`. Ambas URLs sirven el mismo objeto; las URLs ya guardadas siguen funcionando.

---

## Opción A: bucket policy pública solo para el prefijo

### Bucket policy exacta

Reemplaza `<bucket>`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadEmailImagenes",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::<bucket>/email/imagenes/*"
    }
  ]
}
```

Solo concede `s3:GetObject` (no `ListBucket`). Por eso una clave inexistente bajo el prefijo responde
**403** (no 404) y nadie puede listar el contenido.

### Paso 1: Object Ownership = Bucket owner enforced (ACL deshabilitados)

```bash
aws s3api get-bucket-ownership-controls --bucket "$BUCKET" --region "$REGION"
# Debe mostrar "ObjectOwnership": "BucketOwnerEnforced"
```

Si no lo está (revisa antes que ningún cliente dependa de ACL de objetos):

```bash
aws s3api put-bucket-ownership-controls --bucket "$BUCKET" --region "$REGION" \
  --ownership-controls 'Rules=[{ObjectOwnership=BucketOwnerEnforced}]'
```

### Paso 2: aplicar con el script

```bash
export BUCKET=<bucket> REGION=us-east-1          # y AWS_PROFILE=<perfil-admin> si aplica
./docs/s3-imagenes-publicas.sh verificar          # solo lee: ownership, block public access, policy actual
./docs/s3-imagenes-publicas.sh aplicar
```

`aplicar` hace un respaldo de la policy y del Block Public Access en `./s3-respaldo-<bucket>/`, y luego:

```bash
# 1) Permite policies públicas; los ACL siguen bloqueados
aws s3api put-public-access-block --bucket "$BUCKET" --region "$REGION" \
  --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=false,RestrictPublicBuckets=false"

# 2) Agrega el statement PublicReadEmailImagenes a la policy existente (no borra los demás statements)
aws s3api put-bucket-policy --bucket "$BUCKET" --region "$REGION" --policy file://policy.json
```

> `put-bucket-policy` **reemplaza** la policy completa. El script fusiona el statement con los que ya
> existan. Si lo haces a mano, parte de `get-bucket-policy` y agrega el statement.

Si la cuenta tiene **Block Public Access a nivel de cuenta** con `BlockPublicPolicy=true` o
`RestrictPublicBuckets=true`, la policy será rechazada o ignorada. `verificar` lo muestra. En ese caso
usa la opción B (CloudFront), que no necesita abrir el bucket.

### Revertir

```bash
./docs/s3-imagenes-publicas.sh revertir
```

Quita solo el statement `PublicReadEmailImagenes` (o borra la policy si queda vacía) y vuelve a activar
las cuatro opciones de Block Public Access. Los respaldos de `./s3-respaldo-<bucket>/` permiten restaurar
el estado exacto anterior con `put-bucket-policy` / `put-public-access-block`.

**Ojo:** al revertir, las imágenes ya enviadas en correos dejan de verse.

---

## Opción B (recomendada en producción): CloudFront con OAC

El bucket sigue **100% privado** y CloudFront sirve solo el prefijo, con HTTPS, caché en el borde y
un dominio propio si lo deseas.

1. **Origin Access Control**: CloudFront → *Origin access* → *Create control setting*
   (tipo S3, *Sign requests (recommended)*).
2. **Distribución**:
   - Origin: `<bucket>.s3.<region>.amazonaws.com` (el endpoint REST, no el de *website*), con el OAC del paso 1.
   - Default cache behavior: `GET, HEAD`, *Redirect HTTP to HTTPS*, cache policy `CachingOptimized`
     (respeta el `Cache-Control` de un año de los objetos).
   - Opcional: un CNAME (p. ej. `img.essenza.co`) con certificado de ACM en `us-east-1`.
3. **Bucket policy** (CloudFront la propone al crear la distribución). Restríngela al prefijo:

   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "CloudFrontReadEmailImagenes",
         "Effect": "Allow",
         "Principal": { "Service": "cloudfront.amazonaws.com" },
         "Action": "s3:GetObject",
         "Resource": "arn:aws:s3:::<bucket>/email/imagenes/*",
         "Condition": {
           "StringEquals": { "AWS:SourceArn": "arn:aws:cloudfront::<account-id>:distribution/<distribution-id>" }
         }
       }
     ]
   }
   ```

   Con esta policy, cualquier ruta fuera de `email/imagenes/` responde 403 también en CloudFront.
4. Si antes aplicaste la opción A, **quítala** (`./docs/s3-imagenes-publicas.sh revertir`): con OAC el
   Block Public Access puede quedar completo.
5. En **notificacion** configura `IMAGENES_URL_PUBLICA_BASE=https://<dxxxx>.cloudfront.net`
   (o tu CNAME). notificacion arma la URL como `<base>/<key>`, así que no hace falta cambiar este servicio.

Antes de revertir la opción A, prueba en paralelo que `https://<dxxxx>.cloudfront.net/<key>` responde 200
con el `Content-Type` y el `Cache-Control` correctos. Los correos ya enviados apuntan a la URL de S3:
mientras existan correos con esa URL, mantener la opción A evita imágenes rotas.

---

## Registro del bucket en `bucket_aws`

notificacion necesita el `id` de ese registro en `IMAGENES_S3_BUCKET_AWS_ID`. Al subir, este servicio
comprueba que el parámetro `bucket` sea igual al `bucket_name` registrado para ese id.

### SQL idempotente (MySQL)

```sql
SET @bucket_name    = '<bucket>';   -- nombre real del bucket
SET @application_id = NULL;         -- id de la aplicación notificacion en access_control, si lo usan

INSERT INTO bucket_aws (project_name, application_id, bucket_name, bucket_folder, usr_crea, created_at, deleted)
SELECT 'notificacion-correos', @application_id, @bucket_name, 'email/imagenes', 1, UTC_TIMESTAMP(), 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM bucket_aws WHERE project_name = 'notificacion-correos' AND deleted = 0
);

-- id para IMAGENES_S3_BUCKET_AWS_ID
SELECT id, project_name, bucket_name, bucket_folder
FROM bucket_aws
WHERE project_name = 'notificacion-correos' AND deleted = 0;
```

### Alternativa: endpoint (requiere token si `AWS_SERVICES_ENFORCE_AUTH=true`)

Este endpoint **no** es idempotente: consulta primero.

```bash
curl -s "http://localhost:8082/api/aws/bucket_aws/informacion_bucket?nombre_proyecto=notificacion-correos" \
  -H "Authorization: Bearer $TOKEN"
# Si la respuesta viene vacía:
curl -s -X POST http://localhost:8082/api/aws/bucket_aws \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"projectName":"notificacion-correos","applicationId":1,"bucketName":"<bucket>","bucketFolder":"email/imagenes"}'
```

---

## Seguridad

- `subir_archivo` y `eliminar_archivo` **no** deben aparecer en `AWS_SERVICES_RUTAS_PUBLICAS` ni en
  `AWS_SERVICES_RUTAS_PUBLICAS_GET`. Con `AWS_SERVICES_ENFORCE_AUTH=true`, sin token válido responden 401.
- El token de `notificacion-service` debe traer `aud` = `amazon-services-essenza`, que está en
  `AWS_SERVICES_OAUTH2_AUDIENCES` por defecto.
- Hoy no se exige el scope `files.write` por endpoint. Cualquier token válido para la audiencia puede subir o borrar.

## Prueba manual con `curl`

Requisitos: access_control en `http://localhost:8000`, este servicio en `http://localhost:8082` con
`AWS_SERVICES_ENFORCE_AUTH=true`, la opción A aplicada y el registro de `bucket_aws` creado. Usa `jq`.

```bash
export BUCKET=<bucket> REGION=us-east-1 BUCKET_ID=<id de bucket_aws>
export CLIENT_SECRET=<secreto de notificacion-service>
API=http://localhost:8082/api/aws/files-amazon

# 1) Token client_credentials
TOKEN=$(curl -s -X POST http://localhost:8000/oauth2/token \
  -u "notificacion-service:$CLIENT_SECRET" \
  -d grant_type=client_credentials -d scope=files.write | jq -r .access_token)
# Revisa que aud incluya amazon-services-essenza
echo "$TOKEN" | cut -d. -f2 | tr '_-' '/+' | base64 -d 2>/dev/null | jq '{aud, scope, sub}'

# 2) PNG de prueba (1x1) con nombre <uuid>.png
NOMBRE="$(uuidgen | tr 'A-Z' 'a-z').png"
echo 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==' \
  | base64 -d > "$NOMBRE"

# 3) Sin token: debe dar 401
curl -s -o /dev/null -w '%{http_code}\n' -X POST "$API/subir_archivo" \
  -F "file=@$NOMBRE;type=image/png" -F "bucket=$BUCKET" -F "carpeta=email/imagenes/$(date +%Y/%m)" -F "bucket_aws_id=$BUCKET_ID"

# 4) Subir: 200 con id, key y s3Url
RESP=$(curl -s -X POST "$API/subir_archivo" -H "Authorization: Bearer $TOKEN" \
  -F "file=@$NOMBRE;type=image/png" -F "bucket=$BUCKET" -F "carpeta=email/imagenes/$(date +%Y/%m)" -F "bucket_aws_id=$BUCKET_ID")
echo "$RESP" | jq '{id, key, s3Url}'
KEY=$(echo "$RESP" | jq -r .key); URL=$(echo "$RESP" | jq -r .s3Url)

# 5) Abrir la URL SIN credenciales: 200, content-type image/png,
#    cache-control public, max-age=31536000, immutable y content-disposition inline
curl -sI "$URL" | grep -iE '^(HTTP|content-type|cache-control|content-disposition)'

# 6) Validaciones: ambas deben dar 400
curl -s -o /dev/null -w '%{http_code}\n' -X POST "$API/subir_archivo" -H "Authorization: Bearer $TOKEN" \
  -F "file=@$NOMBRE;type=image/png" -F "bucket=$BUCKET" -F "carpeta=email/../privado" -F "bucket_aws_id=$BUCKET_ID"
curl -s -o /dev/null -w '%{http_code}\n' -X POST "$API/subir_archivo" -H "Authorization: Bearer $TOKEN" \
  -F "file=@$NOMBRE;type=image/png" -F "bucket=otro-bucket" -F "carpeta=email/imagenes/$(date +%Y/%m)" -F "bucket_aws_id=$BUCKET_ID"

# 7) Objeto FUERA del prefijo: se sube bien, pero su URL sin credenciales debe dar 403
RESP_PRIV=$(curl -s -X POST "$API/subir_archivo" -H "Authorization: Bearer $TOKEN" \
  -F "file=@$NOMBRE;type=image/png" -F "bucket=$BUCKET" -F "carpeta=pruebas/privado" -F "bucket_aws_id=$BUCKET_ID")
curl -s -o /dev/null -w '%{http_code}\n' "$(echo "$RESP_PRIV" | jq -r .s3Url)"
curl -s -X DELETE -H "Authorization: Bearer $TOKEN" \
  "$API/eliminar_archivo?clave=$(echo "$RESP_PRIV" | jq -r .key)&bucketAwsId=$BUCKET_ID" > /dev/null

# 8) Borrar: 200; repetir: 404; la URL pública deja de responder (403)
curl -s -o /dev/null -w '%{http_code}\n' -X DELETE -H "Authorization: Bearer $TOKEN" \
  "$API/eliminar_archivo?clave=$KEY&bucketAwsId=$BUCKET_ID"
curl -s -o /dev/null -w '%{http_code}\n' -X DELETE -H "Authorization: Bearer $TOKEN" \
  "$API/eliminar_archivo?clave=$KEY&bucketAwsId=$BUCKET_ID"
curl -s -o /dev/null -w '%{http_code}\n' "$URL"
```

La clave va en la query sin codificar porque solo tiene `[a-z0-9-/.]`. Si alguna vez lleva otros
caracteres, usa `--data-urlencode` con `-G`.

En la base de datos, la fila borrada debe quedar con `deleted = 1`:

```sql
SELECT id_files_amazon_s3, `key`, deleted FROM amazon_s3_files WHERE `key` = '<KEY>';
```

## Variables de entorno

| Variable | Servicio | Default | Descripción |
|---|---|---|---|
| `AWS_S3_PREFIJOS_PUBLICOS` | amazon_services_essenza | `email/imagenes/` | Prefijos (separados por coma) que se suben con caché de un año e `inline` |
| `AWS_REGION` | amazon_services_essenza | `us-east-1` | Región usada en la `s3Url` regional |
| `AWS_SERVICES_ENFORCE_AUTH` | amazon_services_essenza | `false` | Debe ser `true` en producción |
| `IMAGENES_S3_BUCKET_AWS_ID` | notificacion | — | `id` del registro `notificacion-correos` en `bucket_aws` |
| `IMAGENES_URL_PUBLICA_BASE` | notificacion | — | Solo con CloudFront (opción B) |
