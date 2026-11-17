package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsByApplicationIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsByProjectNameUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.CreateBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.UpdateBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.DeleteBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.CreateBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.UpdateBucketAwsDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bucket_aws")
@Description(value = "Gestiona parametros de bucket de Amazon s3")
@Tag(name = "Amazon S3 - Buckets")
public class BucketAwsController {

    private final FindBucketAwsByProjectNameUseCase findByProjectName;
    private final FindBucketAwsByApplicationIdUseCase findByApplicationId;
    private final CreateBucketAwsUseCase createBucketAws;
    private final UpdateBucketAwsUseCase updateBucketAws;
    private final DeleteBucketAwsUseCase deleteBucketAws;
    private final FindBucketAwsByIdUseCase findBucketAwsById;
    private final FindBucketAwsUseCase findBucketAws;

    @PostMapping
    @Operation(summary = "Crear un bucket lógico de Amazon S3")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bucket creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<BucketAwsDto> crearBucket(@Valid @RequestBody CreateBucketAwsDto dto) {
        BucketAwsDto created = createBucketAws.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar metadatos de un bucket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bucket actualizado"),
            @ApiResponse(responseCode = "404", description = "Bucket no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<BucketAwsDto> actualizarBucket(@PathVariable("id") Long id,
                                                       @Valid @RequestBody UpdateBucketAwsDto dto) {
        BucketAwsDto updated = updateBucketAws.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un bucket por su identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bucket eliminado"),
            @ApiResponse(responseCode = "404", description = "Bucket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<Void> eliminarBucket(@PathVariable("id") Long id) {
        boolean deleted = deleteBucketAws.deleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener bucket por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bucket encontrado"),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<BucketAwsDto> informacionBucketById(@PathVariable("id") Long id) {
        return findBucketAwsById.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/informacion_bucket")
    @Operation(summary = "Consultar bucket por nombre de proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bucket encontrado"),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public BucketAwsDto informacionBucket(
            @Parameter(description = "Es el nombre del proyecto ") @RequestParam(defaultValue = "0", value = "nombre_proyecto") String nombreProyecto) {

        return findByProjectName.findByProjectName(nombreProyecto).orElse(null);
    }

    @GetMapping("/informacion_bucket_by_aplicacion_id")
    @Operation(summary = "Consultar buckets por ID de aplicación (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public Page<BucketAwsDto> informacionBucketByAplicacionId(
            @Parameter(description = "Es el id de la aplicación ") @RequestParam(value = "aplicacion_id") Integer aplicacionId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        return findByApplicationId.findByApplicationId(aplicacionId, pageable);
    }



    @GetMapping("/listar_buckets")
    @Operation(summary = "Listar todos los buckets registrados (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public Page<BucketAwsDto> listarBuckets(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return findBucketAws.findAll(pageable);
    }


}
