package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsByApplicationIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsByProjectNameUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.CreateBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.UpdateBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.DeleteBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.CreateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.UpdateBuketAwsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/buket_aws")
@Description(value = "Gestiona parametros de buket de Amazon s3")
@Tag(name = "Amazon S3 - Buckets")
public class BuketAwsController {

    private final FindBuketAwsByProjectNameUseCase findByProjectName;
    private final FindBuketAwsByApplicationIdUseCase findByApplicationId;
    private final CreateBuketAwsUseCase createBuketAws;
    private final UpdateBuketAwsUseCase updateBuketAws;
    private final DeleteBuketAwsUseCase deleteBuketAws;
    private final FindBuketAwsByIdUseCase findBuketAwsById;
    private final FindBuketAwsUseCase findBuketAws;

//    public BuketAwsController(FindBuketAwsByProjectNameUseCase findByProjectName,
//                              FindBuketAwsByApplicationIdUseCase findByApplicationId,
//                              CreateBuketAwsUseCase createBuketAws,
//                              UpdateBuketAwsUseCase updateBuketAws,
//                              DeleteBuketAwsUseCase deleteBuketAws,
//                              FindBuketAwsByIdUseCase findBuketAwsById,
//                              FindBuketAwsUseCase findBuketAws) {
//        this.findByProjectName = findByProjectName;
//        this.findByApplicationId = findByApplicationId;
//        this.createBuketAws = createBuketAws;
//        this.updateBuketAws = updateBuketAws;
//        this.deleteBuketAws = deleteBuketAws;
//        this.findBuketAwsById = findBuketAwsById;
//        this.findBuketAws = findBuketAws;
//    }

    @PostMapping
    @Operation(summary = "Crear un bucket lógico de Amazon S3")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bucket creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<BuketAwsDto> crearBuket(@RequestBody CreateBuketAwsDto dto) {
        BuketAwsDto created = createBuketAws.create(dto);
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
    public ResponseEntity<BuketAwsDto> actualizarBuket(@PathVariable("id") Long id,
                                                       @RequestBody UpdateBuketAwsDto dto) {
        BuketAwsDto updated = updateBuketAws.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un bucket por su identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bucket eliminado"),
            @ApiResponse(responseCode = "404", description = "Bucket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<Void> eliminarBuket(@PathVariable("id") Long id) {
        boolean deleted = deleteBuketAws.deleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener bucket por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bucket encontrado"),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<BuketAwsDto> informacionBuketById(@PathVariable("id") Long id) {
        return findBuketAwsById.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/informacion_buket")
    @Operation(summary = "Consultar bucket por nombre de proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bucket encontrado"),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public BuketAwsDto informacionBuket(
            @Parameter(description = "Es el nombre del proyecto ") @RequestParam(defaultValue = "0", value = "nombre_proyecto") String nombreProyecto) {

        return findByProjectName.findByProjectName(nombreProyecto).orElse(null);
    }

    @GetMapping("/informacion_buket_by_aplicacion_id")
    @Operation(summary = "Consultar bucket por ID de aplicación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bucket encontrado"),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public BuketAwsDto informacionBuketByAplicacionId(
            @Parameter(description = "Es el id de la aplicación ") @RequestParam(defaultValue = "0", value = "aplicacion_id") Integer aplicacionId) {

        return findByApplicationId.findByApplicationId(aplicacionId).stream().findFirst().orElse(null);
    }



    @GetMapping("/listar_bukets")
    @Operation(summary = "Listar todos los buckets registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "204", description = "Sin datos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public List<BuketAwsDto> listarBukets() {
        return findBuketAws.findAll();
    }


}
