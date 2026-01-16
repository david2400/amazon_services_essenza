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
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByRelationTypeIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByBucketAwsIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByParentIdUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files-amazon")
@Description(value = "Gestión de archivos de Amazon S3")
public class FilesAmazonS3Controller {

    private final String string = "Error interno en el servidor";

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final CreatePresignedGetUrlUseCase createPresignedGetUrlUseCase;
    private final ReplaceFileUseCase replaceFileUseCase;
    private final UpdateDocumentIdUseCase updateDocumentIdUseCase;
    private final DeleteFileByKeyAndBucketUseCase deleteFileByKeyAndBucketUseCase;
    private final UpdateFilesAmazonS3UseCase updateFilesAmazonS3UseCase;
    private final FindFilesAmazonS3UseCase findFilesAmazonS3UseCase;
    private final FindFilesAmazonS3ByDocumentIdUseCase findByDocumentIdUseCase;
    private final FindFilesAmazonS3ByRelationTypeIdUseCase findByRelationTypeIdUseCase;
    private final FindFilesAmazonS3ByBucketAwsIdUseCase findByBucketAwsIdUseCase;
    private final FindFilesAmazonS3ByParentIdUseCase findByParentIdUseCase;

    private static final Logger logger = LoggerFactory.getLogger(FilesAmazonS3Controller.class);

    public FilesAmazonS3Controller(UploadFileUseCase uploadFileUseCase,
                                   DownloadFileUseCase downloadFileUseCase,
                                   CreatePresignedGetUrlUseCase createPresignedGetUrlUseCase,
                                   ReplaceFileUseCase replaceFileUseCase,
                                   UpdateDocumentIdUseCase updateDocumentIdUseCase,
                                   DeleteFileByKeyAndBucketUseCase deleteFileByKeyAndBucketUseCase,
                                   UpdateFilesAmazonS3UseCase updateFilesAmazonS3UseCase,
                                   FindFilesAmazonS3UseCase findFilesAmazonS3UseCase,
                                   FindFilesAmazonS3ByDocumentIdUseCase findByDocumentIdUseCase,
                                   FindFilesAmazonS3ByRelationTypeIdUseCase findByRelationTypeIdUseCase,
                                   FindFilesAmazonS3ByBucketAwsIdUseCase findByBucketAwsIdUseCase,
                                   FindFilesAmazonS3ByParentIdUseCase findByParentIdUseCase
    ) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.downloadFileUseCase = downloadFileUseCase;
        this.createPresignedGetUrlUseCase = createPresignedGetUrlUseCase;
        this.replaceFileUseCase = replaceFileUseCase;
        this.updateDocumentIdUseCase = updateDocumentIdUseCase;
        this.deleteFileByKeyAndBucketUseCase = deleteFileByKeyAndBucketUseCase;
        this.updateFilesAmazonS3UseCase = updateFilesAmazonS3UseCase;
        this.findFilesAmazonS3UseCase = findFilesAmazonS3UseCase;
        this.findByDocumentIdUseCase = findByDocumentIdUseCase;
        this.findByRelationTypeIdUseCase = findByRelationTypeIdUseCase;
        this.findByBucketAwsIdUseCase = findByBucketAwsIdUseCase;
        this.findByParentIdUseCase = findByParentIdUseCase;
//        this.requestHttp = requestHttp;
    }

    @PostMapping("/subir_archivo")
    @Operation(summary = "Permite subir archivos a Amazon S3", responses = {
            @ApiResponse(description = "Archivo se ha subido con exito", responseCode = "200"),
            @ApiResponse(description = "Falta parametro de entrada, ya se archivo o nombre de bucket", responseCode = "400"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")})
    public FilesAmazonS3Dto subirArchivo(
            @Parameter(description = "Es el archivo a subir") @RequestParam(value = "file") MultipartFile multipartFile,
            @Parameter(description = "Es el nombre de bucket que esta creado en Amazon S3") @RequestParam(defaultValue = "0", value = "bucket") String bucket,
            @Parameter(description = "Es el nombre de la carpeta (si existe) donde se alojará el archivo en Amazon S3") @RequestParam(defaultValue = "0", value = "carpeta") String carpeta,
            @Parameter(description = "Es el id de tabla donde están los nombres de los bukets") @RequestParam(defaultValue = "0", value = "buket_aws_id") Long buketAwsId,
            @Parameter(description = "Es la llave del documento (Normalmente es la cédula de la persona)") @RequestParam(value = "llave", required = false) String llave,
            @Parameter(description = "Es el tipo de documento") @RequestParam(value = "documento_id", required = false) Integer documentoId,
            @Parameter(description = "Es la referencia del documento") @RequestParam(value = "referencia", required = false) Integer referencia,
            @Parameter(description = "Es el tipo de relacion") @RequestParam(value = "tipo_relacion_id", required = false) Integer tipoRelacionId,
            @Parameter(description = "Es el id padre de tabla ArchivoAmazons3") @RequestParam(value = "id_padre", required = false) Long idPadre,
            HttpServletRequest request) {

        FilesAmazonS3Dto result;

        if (multipartFile.isEmpty()) {

            throw new InternalException("Falta parametro de entrada tipo file");

        } else if (bucket.equals("0")) {

            throw new InternalException("Falta el nombre de bucket");
        }

        carpeta = carpeta.equals("0") ? "" : carpeta; //Si carpeta es 0, archivo no se debe de guardar en carpeta


        try {
            // Armar metadatos para crear el registro
            CreateFilesAmazonS3Dto meta = CreateFilesAmazonS3Dto.builder()
                    .parentId(idPadre)
                    .documentId(documentoId)
                    .relationTypeId(tipoRelacionId)
                    .reference(referencia)
                    .bucketAwsId(buketAwsId)
                    .key(llave)
                    .build();

//            String userId = requestHttp.getCedulaUsuario(request);
            String userId = "0";

            result = uploadFileUseCase.upload(multipartFile, bucket, carpeta, meta, userId);

            String log = "Archivo se ha subido con exito.";

            logger.info(log);

        } catch (Exception e) {

            e.printStackTrace();

            throw new InternalError(e.getMessage());

        }

        return result;

    }

    @GetMapping("/descargar_archivo")
    @Operation(summary = "Permite descargar archivos de Amazon S3",
            description = "Se debe enviar la clave (key) y el bucket",
            responses = {
                    @ApiResponse(description = "Archivo se ha descargado con exito", responseCode = "200"),
                    @ApiResponse(description = "Error interno en el servidor", responseCode = "500")
            })
    public ResponseEntity<Resource> descargarArchivo(@RequestParam("bucket") String bucket,
                                                     @RequestParam("clave") String clave,
                                                     @RequestParam(value = "nombreArchivo", required = false) String nombreArchivo) {

        byte[] bs = downloadFileUseCase.download(bucket, clave);
        HttpHeaders header = new HttpHeaders();
        if (nombreArchivo != null && !nombreArchivo.isEmpty()) {
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo);
        }
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        ByteArrayResource arrayResource = new ByteArrayResource(bs);
        return ResponseEntity.ok().headers(header).contentLength(bs.length)
                .contentType(MediaType.parseMediaType("application/octet-stream")).body(arrayResource);
    }

    @GetMapping("/crear_url")
    @Operation(summary = "Crear URL presignada de descarga",
            description = "Genera una URL temporal para descargar un objeto",
            responses = {
                    @ApiResponse(description = "URL generada con exito", responseCode = "200"),
                    @ApiResponse(description = "Error interno en el servidor", responseCode = "500")
            })
    public String crearUrl(@RequestParam("bucket") String bucket,
                           @RequestParam("clave") String clave,
                           @RequestParam(value = "minutos", defaultValue = "10") int minutos) {
        return createPresignedGetUrlUseCase.createPresignedGetUrl(bucket, clave, java.time.Duration.ofMinutes(minutos));
    }

    @GetMapping("/informacion_archivo")
    @Operation(summary = "Información de archivos",
            description = "Consulta archivos por parámetros opcionales o lista todo si no hay filtros")
    public List<FilesAmazonS3Dto> informacionArchivo(
            @RequestParam(value = "documento_id", required = false) Integer documentoId,
            @RequestParam(value = "tipo_relacion_id", required = false) Integer tipoRelacionId,
            @RequestParam(value = "buket_aws_id", required = false) Long buketAwsId,
            @RequestParam(value = "id_padre", required = false) Long idPadre) {

        if (documentoId != null) return findByDocumentIdUseCase.findByDocumentId(documentoId);
        if (tipoRelacionId != null) return findByRelationTypeIdUseCase.findByRelationTypeId(tipoRelacionId);
        if (buketAwsId != null) return findByBucketAwsIdUseCase.findByBucketAwsId(buketAwsId);
        if (idPadre != null) return findByParentIdUseCase.findByParentId(idPadre);
        return findFilesAmazonS3UseCase.findAll();
    }

    @PutMapping("/actualizar/{id}")
    @Operation(summary = "Actualizar metadatos del archivo")
    public FilesAmazonS3Dto actualizar(@PathVariable("id") Long id,
                                       @RequestBody UpdateFilesAmazonS3Dto dto) {
        return updateFilesAmazonS3UseCase.update(id, dto);
    }

    @DeleteMapping("/eliminar_archivo")
    @Operation(summary = "Eliminar archivo por clave y bucketAwsId", responses = {
            @ApiResponse(description = "Archivo eliminado con exito", responseCode = "200"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")})
    public List<FilesAmazonS3Dto> eliminarArchivo(
            @RequestParam("clave") String clave,
            @RequestParam("buketAwsId") Long buketAwsId,
            HttpServletRequest httpServletRequest) {

//        String userId = requestHttp.getCedulaUsuario(httpServletRequest);
        String userId = "0";
        return deleteFileByKeyAndBucketUseCase.deleteByKeyAndBucket(clave, buketAwsId, userId);
    }


    @PutMapping("/remplazar_archivo/{id}")
    @Operation(summary = "Reemplazar archivo en Amazon S3", responses = {
            @ApiResponse(description = "Archivo reemplazado con exito", responseCode = "200"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")})
    public FilesAmazonS3Dto remplazarArchivo(
            @PathVariable("id") Long id,
            @Parameter(description = "Es el archivo a subir") @RequestParam(value = "file") MultipartFile multipartFile
    ) {
        FilesAmazonS3Dto result = replaceFileUseCase.replace(id, multipartFile);
        logger.info("Archivo reemplazado con exito: {}", result.getFileName());
        return result;
    }

    @PutMapping("/actualizar_documento/{id}/{documentoId}")
    @Operation(summary = "Actualizar documentoId de un archivo", responses = {
            @ApiResponse(description = "Archivo actualizado con éxito", responseCode = "200"),
            @ApiResponse(description = "Error interno en el servidor", responseCode = "500")
    })
    public ResponseEntity<FilesAmazonS3Dto> actualizarDocumentoId(
            @PathVariable("id") Long id,
            @PathVariable("documentoId") Integer documentoId,
            HttpServletRequest request) {

//        String userId = requestHttp.getCedulaUsuario(request);
        String userId = "";
        FilesAmazonS3Dto resultado = updateDocumentIdUseCase.updateDocumentId(id, documentoId, userId);
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }
}
