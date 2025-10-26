package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buket_aws")
@Description(value = "Gestiona parametros de buket de Amazon s3")
//@Api(tags = "Gestion Buket")
public class BuketAwsController {

    private final FindBuketAwsByProjectNameUseCase findByProjectName;
    private final FindBuketAwsByApplicationIdUseCase findByApplicationId;
    private final CreateBuketAwsUseCase createBuketAws;
    private final UpdateBuketAwsUseCase updateBuketAws;
    private final DeleteBuketAwsUseCase deleteBuketAws;
    private final FindBuketAwsByIdUseCase findBuketAwsById;
    private final FindBuketAwsUseCase findBuketAws;

    public BuketAwsController(FindBuketAwsByProjectNameUseCase findByProjectName,
                              FindBuketAwsByApplicationIdUseCase findByApplicationId,
                              CreateBuketAwsUseCase createBuketAws,
                              UpdateBuketAwsUseCase updateBuketAws,
                              DeleteBuketAwsUseCase deleteBuketAws,
                              FindBuketAwsByIdUseCase findBuketAwsById,
                              FindBuketAwsUseCase findBuketAws) {
        this.findByProjectName = findByProjectName;
        this.findByApplicationId = findByApplicationId;
        this.createBuketAws = createBuketAws;
        this.updateBuketAws = updateBuketAws;
        this.deleteBuketAws = deleteBuketAws;
        this.findBuketAwsById = findBuketAwsById;
        this.findBuketAws = findBuketAws;
    }

    @PostMapping
    public ResponseEntity<BuketAwsDto> crearBuket(@RequestBody CreateBuketAwsDto dto) {
        BuketAwsDto created = createBuketAws.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuketAwsDto> actualizarBuket(@PathVariable("id") Long id,
                                                       @RequestBody UpdateBuketAwsDto dto) {
        BuketAwsDto updated = updateBuketAws.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBuket(@PathVariable("id") Long id) {
        boolean deleted = deleteBuketAws.deleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuketAwsDto> informacionBuketById(@PathVariable("id") Long id) {
        return findBuketAwsById.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/informacion_buket")
    public BuketAwsDto informacionBuket(
            @Parameter(description = "Es el nombre del proyecto ") @RequestParam(defaultValue = "0", value = "nombre_proyecto") String nombreProyecto) {

        return findByProjectName.findByProjectName(nombreProyecto).orElse(null);
    }

    @GetMapping("/informacion_buket_by_aplicacion_id")
    public BuketAwsDto informacionBuketByAplicacionId(
            @Parameter(description = "Es el id de la aplicación ") @RequestParam(defaultValue = "0", value = "aplicacion_id") Integer aplicacionId) {

        return findByApplicationId.findByApplicationId(aplicacionId).stream().findFirst().orElse(null);
    }



    @GetMapping("/listar_bukets")
    public List<BuketAwsDto> listarBukets() {
        return findBuketAws.findAll();
    }


}
