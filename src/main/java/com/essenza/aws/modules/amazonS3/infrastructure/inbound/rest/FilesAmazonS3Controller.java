package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UploadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DownloadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.CreatePresignedGetUrlUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.ReplaceFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DeleteFileByKeyAndBucketUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByBucketAwsIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByParentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.LinkFileToBucketUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.LinkFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files-amazon")
@Description(value = "Gestión de archivos de Amazon S3")
@Tag(name = "Amazon S3 - Archivos")
@Validated
@Slf4j
public class FilesAmazonS3Controller {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final CreatePresignedGetUrlUseCase createPresignedGetUrlUseCase;
    private final ReplaceFileUseCase replaceFileUseCase;
    private final UpdateDocumentIdUseCase updateDocumentIdUseCase;
    private final DeleteFileByKeyAndBucketUseCase deleteFileByKeyAndBucketUseCase;
    private final UpdateFilesAmazonS3UseCase updateFilesAmazonS3UseCase;
    private final FindFilesAmazonS3UseCase findFilesAmazonS3UseCase;
    private final FindFilesAmazonS3ByDocumentIdUseCase findByDocumentIdUseCase;
    private final FindFilesAmazonS3ByBucketAwsIdUseCase findByBucketAwsIdUseCase;
    private final FindFilesAmazonS3ByParentIdUseCase findByParentIdUseCase;
    private final LinkFileToBucketUseCase linkFileToBucketUseCase;

    @PostMapping("/subir_archivo")
    @Operation(summary = "Permite subir archivos a Amazon S3", responses = {
            @ApiResponse(description = "Archivo se ha subido con exito", responseCode = "200"),
            @ApiResponse(description = "Falta parametro de entrada, ya se archivo o nombre de bucket", responseCode = "400"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")})
    public FilesAmazonS3Dto subirArchivo(
            @Parameter(description = "Es el archivo a subir") @RequestParam(value = "file") @NotNull MultipartFile multipartFile,
            @Parameter(description = "Es el nombre de bucket que esta creado en Amazon S3") @RequestParam(value = "bucket") @NotBlank String bucket,
            @Parameter(description = "Es el nombre de la carpeta (si existe) donde se alojará el archivo en Amazon S3") @RequestParam(value = "carpeta", required = false) String carpeta,
            @Parameter(description = "Es el id de tabla donde están los nombres de los buckets") @RequestParam(value = "bucket_aws_id") @NotNull @Min(1) Long bucketAwsId,
            @Parameter(description = "Es el tipo de documento") @RequestParam(value = "documento_id", required = false) Integer documentoId,
            @Parameter(description = "Es la referencia del documento") @RequestParam(value = "referencia", required = false) Integer referencia,
            @Parameter(description = "Es el id padre de tabla ArchivoAmazons3") @RequestParam(value = "id_padre", required = false) Long idPadre) {

        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo a subir no puede estar vacío");
        }

        String normalizedFolder = StringUtils.hasText(carpeta) ? carpeta : "";

        CreateFilesAmazonS3Dto meta = CreateFilesAmazonS3Dto.builder()
                .parentId(idPadre)
                .documentId(documentoId)
                .reference(referencia)
                .bucketAwsId(bucketAwsId)
                .build();

        FilesAmazonS3Dto result = uploadFileUseCase.upload(multipartFile, bucket, normalizedFolder, meta);
        log.info("Archivo subido con exito: {}", result.getKey());
        return result;
    }

    @GetMapping("/descargar_archivo")
    @Operation(summary = "Permite descargar archivos de Amazon S3",
            description = "Se debe enviar la clave (key) y el bucket",
            responses = {
                    @ApiResponse(description = "Archivo se ha descargado con exito", responseCode = "200"),
                    @ApiResponse(description = "Error interno en el servidor", responseCode = "500")
            })
    public ResponseEntity<StreamingResponseBody> descargarArchivo(@RequestParam("bucket") @NotBlank String bucket,
                                                                  @RequestParam("clave") @NotBlank String clave,
                                                                  @RequestParam(value = "nombreArchivo", required = false) String nombreArchivo) {

        return downloadResponse(downloadFileUseCase.download(bucket, clave), nombreArchivo);
    }

    @GetMapping("/descargar/{id}")
    @Operation(summary = "Descargar archivo por id de referencia",
            description = "Resuelve internamente el bucket físico; sirve también para referencias vinculadas a objetos de otros proyectos")
    public ResponseEntity<StreamingResponseBody> descargarPorId(@PathVariable("id") @Min(1) Long id,
                                                                @RequestParam(value = "nombreArchivo", required = false) String nombreArchivo) {
        return downloadResponse(downloadFileUseCase.download(id), nombreArchivo);
    }

    @PostMapping("/vincular/{id}")
    @Operation(summary = "Vincular archivo a otro bucket/proyecto",
            description = "Crea una referencia del archivo existente en otro bucket_aws_id sin volver a subir el contenido a S3",
            responses = {
                    @ApiResponse(description = "Archivo vinculado con exito", responseCode = "200"),
                    @ApiResponse(description = "Archivo o bucket destino no encontrado", responseCode = "404")
            })
    public FilesAmazonS3Dto vincular(@PathVariable("id") @Min(1) Long id,
                                     @RequestParam("bucket_aws_id") @NotNull @Min(1) Long bucketAwsId,
                                     @Valid @RequestBody LinkFilesAmazonS3Dto meta) {
        FilesAmazonS3Dto linked = linkFileToBucketUseCase.link(id, bucketAwsId, meta);
        log.info("Archivo {} vinculado al bucket_aws_id {} como referencia {}", id, bucketAwsId, linked.getId());
        return linked;
    }

    @GetMapping("/crear_url")
    @Operation(summary = "Crear URL presignada de descarga",
            description = "Genera una URL temporal para descargar un objeto",
            responses = {
                    @ApiResponse(description = "URL generada con exito", responseCode = "200"),
                    @ApiResponse(description = "Error interno en el servidor", responseCode = "500")
            })
    public String crearUrl(@RequestParam("bucket") @NotBlank String bucket,
                           @RequestParam("clave") @NotBlank String clave,
                           @RequestParam(value = "minutos", defaultValue = "10") @Min(1) int minutos) {
        return createPresignedGetUrlUseCase.createPresignedGetUrl(bucket, clave, Duration.ofMinutes(minutos));
    }

    @GetMapping("/crear_url/{id}")
    @Operation(summary = "URL presignada por id de referencia",
            description = "Genera la URL resolviendo la ubicación física del objeto, sin que el consumidor conozca el bucket real")
    public String crearUrlPorId(@PathVariable("id") @Min(1) Long id,
                                @RequestParam(value = "minutos", defaultValue = "10") @Min(1) int minutos) {
        return createPresignedGetUrlUseCase.createPresignedGetUrl(id, Duration.ofMinutes(minutos));
    }

    @GetMapping("/informacion_archivo")
    @Operation(summary = "Información de archivos",
            description = "Consulta archivos por parámetros opcionales o lista todo si no hay filtros (paginado)")
    public Page<FilesAmazonS3Dto> informacionArchivo(
            @RequestParam(value = "documento_id", required = false) Integer documentoId,
            @RequestParam(value = "bucket_aws_id", required = false) Long bucketAwsId,
            @RequestParam(value = "id_padre", required = false) Long idPadre,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        if (documentoId != null) return findByDocumentIdUseCase.findByDocumentId(documentoId, pageable);
        if (bucketAwsId != null) return findByBucketAwsIdUseCase.findByBucketAwsId(bucketAwsId, pageable);
        if (idPadre != null) return findByParentIdUseCase.findByParentId(idPadre, pageable);
        return findFilesAmazonS3UseCase.findAll(pageable);
    }

    @PutMapping("/actualizar/{id}")
    @Operation(summary = "Actualizar metadatos del archivo")
    public FilesAmazonS3Dto actualizar(@PathVariable("id") Long id,
                                       @Valid @RequestBody UpdateFilesAmazonS3Dto dto) {
        return updateFilesAmazonS3UseCase.update(id, dto);
    }

    @DeleteMapping("/eliminar_archivo")
    @Operation(summary = "Eliminar archivo por clave y bucketAwsId", responses = {
            @ApiResponse(description = "Archivo eliminado con exito", responseCode = "200"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")})
    public FilesAmazonS3Dto eliminarArchivo(
            @RequestParam("clave") @NotBlank String clave,
            @RequestParam("bucketAwsId") @NotNull @Min(1) Long bucketAwsId) {
        return deleteFileByKeyAndBucketUseCase.deleteByKeyAndBucket(clave, bucketAwsId);
    }


    @PutMapping("/remplazar_archivo/{id}")
    @Operation(summary = "Reemplazar archivo en Amazon S3", responses = {
            @ApiResponse(description = "Archivo reemplazado con exito", responseCode = "200"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")})
    public FilesAmazonS3Dto remplazarArchivo(
            @PathVariable("id") @NotNull Long id,
            @Parameter(description = "Es el archivo a subir") @RequestParam(value = "file") @NotNull MultipartFile multipartFile
    ) {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo a subir no puede estar vacío");
        }
        FilesAmazonS3Dto result = replaceFileUseCase.replace(id, multipartFile);
        log.info("Archivo reemplazado con exito: {}", result.getFileName());
        return result;
    }

    @PutMapping("/actualizar_documento/{id}/{documentoId}")
    @Operation(summary = "Actualizar documentoId de un archivo", responses = {
            @ApiResponse(description = "Archivo actualizado con éxito", responseCode = "200"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")
    })
    public ResponseEntity<FilesAmazonS3Dto> actualizarDocumentoId(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("documentoId") @NotNull Integer documentoId) {
        FilesAmazonS3Dto resultado = updateDocumentIdUseCase.updateDocumentId(id, documentoId);
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    private ResponseEntity<StreamingResponseBody> downloadResponse(S3ObjectStream objectStream, String nombreArchivo) {
        HttpHeaders header = new HttpHeaders();
        if (StringUtils.hasText(nombreArchivo)) {
            // ContentDisposition escapa el nombre y añade filename* UTF-8 (evita header injection).
            header.add(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(nombreArchivo, StandardCharsets.UTF_8).build().toString());
        }
        header.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        header.add(HttpHeaders.PRAGMA, "no-cache");
        header.add(HttpHeaders.EXPIRES, "0");

        MediaType contentType = objectStream.getContentType() != null
                ? MediaType.parseMediaType(objectStream.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        StreamingResponseBody body = outputStream -> {
            try (var inputStream = objectStream.getInputStream()) {
                inputStream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(objectStream.getContentLength() != null ? objectStream.getContentLength() : -1)
                .contentType(contentType)
                .body(body);
    }
}
