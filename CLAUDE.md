# CLAUDE.md — amazon_services_essenza

Microservicio Spring Boot que abstrae servicios de AWS (S3, SNS) detrás de una API REST con arquitectura hexagonal y persistencia en MySQL.

## Stack

- Java 21, Spring Boot 3.5.7 (Web, Data JPA, Security, Validation, Actuator)
- AWS SDK v2 (`software.amazon.awssdk` 2.17.142): S3, SNS
- MySQL 8 (`mysql-connector-j`), H2 para tests
- MapStruct 1.5.5 (mappers entity↔DTO, generados por annotation processor)
- Lombok, springdoc-openapi 2.8.9 (Swagger UI)
- `com.essenza:essenza-security` 1.0.0 (lib interna de validación de tokens — requiere `mvn install` previo del repo `essenza-security` con jjwt 0.12.x)

## Comandos

```bash
# Compilar
./mvnw compile

# Empaquetar / verificar completo
./mvnw clean package

# Correr la app
./mvnw spring-boot:run

# Tests
./mvnw test
```

No existe configuración de CI; verificación mínima = `./mvnw compile`.

## Arranque local

- Puerto: **8082**
- Todas las rutas de los módulos llevan prefijo **`/api/aws`** (ver `ApiPrefixConfiguration`). Ej: `POST /api/aws/sns/topics`.
- Swagger UI: `http://localhost:8082/swagger-ui/index.html` (excluido del prefijo).
- Actuator: `http://localhost:8082/actuator` (excluido del prefijo).

### Variables de entorno requeridas

| Variable | Descripción | Default |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL | `localhost` / `3306` / `amazon` |
| `DB_USERNAME` / `DB_PASSWORD` | credenciales MySQL | `root` / **sin default (obligatoria)** |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | credenciales AWS; si vacías se usa `DefaultCredentialsProvider` (env, `~/.aws`, IAM role) | vacío |
| `AWS_REGION` | región AWS | `us-east-1` |

`spring.jpa.hibernate.ddl-auto: update` — las tablas/índices se crean solos al arrancar.

## Arquitectura

Hexagonal (ports & adapters) por módulo bajo `com.essenza.aws.modules.<módulo>`:

```
modules/
  amazonS3/                      # buckets, documents, files, presigned URLs
  amazonSNS/
    application/
      input/                     # puertos IN (un UseCase interface por operación)
      output/repository/         # puertos OUT (interfaces de repositorio)
      services/                  # implementa puertos IN; usa puertos OUT + SnsClient
    domain/dto/                  # DTOs por agregado (topics/subscriptions/notifications)
    infrastructure/
      config/                    # beans del cliente AWS (timeouts, retry)
      inbound/rest/              # controllers REST → usan puertos IN
      outbound/
        mappers/                 # MapStruct (+ JsonMapConverter para Map↔JSON)
        persistence/mysql/       # entidades JPA
        repositories/            # Jpa*Repository + *RepositoryAdapter (implementa puerto OUT)
shared/
  common/domain/                 # AuditInfo (entity) / AuditInfoDto (auditoría READ_ONLY)
  config/AwsProperties.java      # @ConfigurationProperties("aws") tipado
  exceptions/                    # NotFoundException, AwsServiceException, GlobalExceptionHandler
security/                        # Spring Security + filtros de essenza-security
```

### Reglas y convenciones importantes

- **Los servicios inyectan puertos** (`SnsTopicRepository`), nunca los adapters concretos.
- **Nunca envolver llamadas a AWS en `@Transactional`** — AWS se llama fuera de la transacción; los adapters manejan su propia tx. En `publish`, si SNS falla se persiste `status=FAILED` + `errorMessage` antes de lanzar `AwsServiceException`.
- Errores: `NotFoundException` → 404, `AwsServiceException` (envuelve `SnsException` con `awsErrorCode`) → 502, `IllegalArgumentException` → 400. Todo pasa por `GlobalExceptionHandler`.
- Listados paginados con `Page<T>`/`Pageable` (`@PageableDefault(20)`), desde controller hasta JPA.
- DTOs exponen `Map<String,String>` para attributes/tags/messageAttributes; las entidades guardan JSON TEXT vía `JsonMapConverter` (MapStruct `uses=`).
- Entidades extienden `AuditInfo` (`usrCrea`, `usrMod`, `createdAt`, `updatedAt`, soft delete `@SoftDelete`). En `AuditInfoDto` esos campos son `@JsonProperty(READ_ONLY)` — no aceptarlos en requests.
- Validaciones de negocio SNS: ARN regex, `policy` debe ser JSON IAM válido, `messageGroupId`/`messageDeduplicationId` solo en tópicos FIFO (ARN termina `.fifo`), `FifoTopic`/`ContentBasedDeduplication` NO son actualizables (AWS no lo permite).
- Métricas Micrometer: `sns.notifications.published{result=success|error}`.
- SNS `publishBatch` en chunks de 10 (límite de AWS), con tracking por mensaje.

## Gotchas conocidas

- **Credenciales AWS nunca en `application.yml`** — GitHub push protection ya bloqueó un push por esto. Usar env vars o IAM.
- `springdoc-openapi` debe ser ≥2.8.x con Spring Boot 3.5 (2.5.0 rompe con `NoSuchMethodError` en `ControllerAdviceBean`).
- `server.max-http-request-header-size: 32KB` está configurado por cookies grandes en localhost.
- Suscripciones `email`/`sms` quedan en `PENDING` hasta confirmación de AWS; SMS requiere salir del SMS sandbox (números verificados + límite de gasto).
- El paquete usa `amazonSNS`/`amazonS3` (camelCase histórico) — mantener consistencia, no renombrar sin refactor completo.
- `essenza-security` debe estar instalado en `.m2` local (`cd ../essenza-security && mvn install`) o la compilación falla.
