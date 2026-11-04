#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# Lectura pública SOLO del prefijo de imágenes de correo (email/imagenes/) de un bucket S3.
# El resto del bucket sigue privado y los ACL siguen bloqueados.
#
# Lo ejecuta una persona con un perfil de AWS CLI con permisos de administración del bucket.
# NO lo ejecuta la aplicación y no usa las credenciales del servicio.
#
# Uso:
#   BUCKET=mi-bucket REGION=us-east-1 ./docs/s3-imagenes-publicas.sh verificar   # solo lee, no cambia nada
#   BUCKET=mi-bucket REGION=us-east-1 ./docs/s3-imagenes-publicas.sh aplicar
#   BUCKET=mi-bucket REGION=us-east-1 ./docs/s3-imagenes-publicas.sh revertir
#
# Variables:
#   BUCKET   (obligatoria) nombre del bucket
#   REGION   (obligatoria) región del bucket, p. ej. us-east-1
#   PREFIJO  (opcional)    prefijo público, por defecto email/imagenes
#   AWS_PROFILE            perfil de AWS CLI a usar (opcional)
#
# Requisitos: aws CLI v2 y python3 (o python) para fusionar la bucket policy existente.
# -----------------------------------------------------------------------------
set -euo pipefail

ACCION="${1:-verificar}"
: "${BUCKET:?Define BUCKET}"
: "${REGION:?Define REGION}"
PREFIJO="${PREFIJO:-email/imagenes}"
PREFIJO="${PREFIJO#/}"
PREFIJO="${PREFIJO%/}"
SID="PublicReadEmailImagenes"
RESPALDO_DIR="${RESPALDO_DIR:-./s3-respaldo-${BUCKET}}"

PY="$(command -v python3 || command -v python || true)"
[ -n "$PY" ] || { echo "Se necesita python3 para fusionar la bucket policy" >&2; exit 1; }

aws_s3api() { aws s3api --region "$REGION" "$@"; }

politica_actual() {
  aws_s3api get-bucket-policy --bucket "$BUCKET" --query Policy --output text 2>/dev/null || echo ""
}

verificar() {
  echo "== Object Ownership (debe ser BucketOwnerEnforced: ACL deshabilitados)"
  local ownership
  ownership="$(aws_s3api get-bucket-ownership-controls --bucket "$BUCKET" \
      --query 'OwnershipControls.Rules[0].ObjectOwnership' --output text 2>/dev/null || echo "SIN_CONFIGURAR")"
  echo "   $ownership"
  if [ "$ownership" != "BucketOwnerEnforced" ]; then
    echo "   ATENCION: configúralo antes de aplicar (ver docs/S3_IMAGENES_CORREO.md, paso 1)."
  fi

  echo "== Block Public Access del bucket"
  aws_s3api get-public-access-block --bucket "$BUCKET" --output table 2>/dev/null || echo "   (sin configuración a nivel de bucket)"

  echo "== Block Public Access de la cuenta (si BlockPublicPolicy=true aquí, la policy pública será rechazada)"
  local cuenta
  cuenta="$(aws sts get-caller-identity --query Account --output text)"
  aws s3control get-public-access-block --account-id "$cuenta" --region "$REGION" --output table 2>/dev/null \
    || echo "   (sin configuración a nivel de cuenta)"

  echo "== Bucket policy actual"
  local pol
  pol="$(politica_actual)"
  if [ -n "$pol" ] && [ "$pol" != "None" ]; then echo "$pol" | "$PY" -m json.tool; else echo "   (sin policy)"; fi
}

respaldar() {
  mkdir -p "$RESPALDO_DIR"
  local ts; ts="$(date +%Y%m%d-%H%M%S)"
  politica_actual > "$RESPALDO_DIR/policy-$ts.json" || true
  aws_s3api get-public-access-block --bucket "$BUCKET" \
      --query PublicAccessBlockConfiguration > "$RESPALDO_DIR/public-access-block-$ts.json" 2>/dev/null || true
  echo "Respaldo guardado en $RESPALDO_DIR (*-$ts.json)"
}

aplicar() {
  local ownership
  ownership="$(aws_s3api get-bucket-ownership-controls --bucket "$BUCKET" \
      --query 'OwnershipControls.Rules[0].ObjectOwnership' --output text 2>/dev/null || echo "SIN_CONFIGURAR")"
  if [ "$ownership" != "BucketOwnerEnforced" ]; then
    echo "Object Ownership es '$ownership'. Configura BucketOwnerEnforced primero (docs/S3_IMAGENES_CORREO.md, paso 1)." >&2
    exit 1
  fi

  respaldar

  echo "== 1/2 Block Public Access: se permiten policies públicas; los ACL siguen bloqueados"
  aws_s3api put-public-access-block --bucket "$BUCKET" --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=false,RestrictPublicBuckets=false"

  echo "== 2/2 Bucket policy: s3:GetObject público SOLO sobre arn:aws:s3:::$BUCKET/$PREFIJO/*"
  local actual nueva
  actual="$(politica_actual)"
  nueva="$(ACTUAL="$actual" BUCKET="$BUCKET" PREFIJO="$PREFIJO" SID="$SID" "$PY" - <<'PYEOF'
import json, os
actual = os.environ.get("ACTUAL", "").strip()
policy = json.loads(actual) if actual and actual != "None" else {"Version": "2012-10-17", "Statement": []}
statements = [s for s in policy.get("Statement", []) if s.get("Sid") != os.environ["SID"]]
statements.append({
    "Sid": os.environ["SID"],
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": f"arn:aws:s3:::{os.environ['BUCKET']}/{os.environ['PREFIJO']}/*",
})
policy["Statement"] = statements
print(json.dumps(policy))
PYEOF
)"
  aws_s3api put-bucket-policy --bucket "$BUCKET" --policy "$nueva"
  echo "Listo. Policy aplicada:"
  politica_actual | "$PY" -m json.tool
}

revertir() {
  respaldar
  echo "== 1/2 Quitar el statement $SID de la bucket policy (se conservan los demás)"
  local actual nueva
  actual="$(politica_actual)"
  if [ -n "$actual" ] && [ "$actual" != "None" ]; then
    nueva="$(ACTUAL="$actual" SID="$SID" "$PY" - <<'PYEOF'
import json, os
policy = json.loads(os.environ["ACTUAL"])
policy["Statement"] = [s for s in policy.get("Statement", []) if s.get("Sid") != os.environ["SID"]]
print(json.dumps(policy) if policy["Statement"] else "")
PYEOF
)"
    if [ -n "$nueva" ]; then
      aws_s3api put-bucket-policy --bucket "$BUCKET" --policy "$nueva"
    else
      aws_s3api delete-bucket-policy --bucket "$BUCKET"
    fi
  fi

  echo "== 2/2 Block Public Access completo"
  aws_s3api put-public-access-block --bucket "$BUCKET" --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
  echo "Revertido: el bucket vuelve a ser privado."
}

case "$ACCION" in
  verificar) verificar ;;
  aplicar)   aplicar ;;
  revertir)  revertir ;;
  *) echo "Acción desconocida: $ACCION (usa verificar | aplicar | revertir)" >&2; exit 1 ;;
esac
