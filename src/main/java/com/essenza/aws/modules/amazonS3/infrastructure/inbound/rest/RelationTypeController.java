package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonS3.application.input.relation_type.CreateRelationTypeUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relation_type.DeleteRelationTypeUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relation_type.FindRelationTypeByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relation_type.FindRelationTypesUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relation_type.UpdateRelationTypeUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.CreateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.UpdateRelationTypeDto;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/relation_type")
@Description(value = "Tipo relación documentos")
public class RelationTypeController {

    private final CreateRelationTypeUseCase createRelationType;
    private final UpdateRelationTypeUseCase updateRelationType;
    private final DeleteRelationTypeUseCase deleteRelationType;
    private final FindRelationTypeByIdUseCase findRelationTypeById;
    private final FindRelationTypesUseCase findRelationTypes;

    public RelationTypeController(CreateRelationTypeUseCase createRelationType,
                                  UpdateRelationTypeUseCase updateRelationType,
                                  DeleteRelationTypeUseCase deleteRelationType,
                                  FindRelationTypeByIdUseCase findRelationTypeById,
                                  FindRelationTypesUseCase findRelationTypes) {

        this.createRelationType = createRelationType;
        this.updateRelationType = updateRelationType;
        this.deleteRelationType = deleteRelationType;
        this.findRelationTypeById = findRelationTypeById;
        this.findRelationTypes = findRelationTypes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RelationTypeDto createTipoRelacion(@RequestBody CreateRelationTypeDto tipoRelacionDto, HttpServletRequest httpServletRequest) {
        return createRelationType.create(tipoRelacionDto);
    }

    @PutMapping("/{id}")
    public RelationTypeDto updateTipoRelacion(@PathVariable("id") Integer id, @RequestBody UpdateRelationTypeDto tipoRelacionDto, HttpServletRequest httpServletRequest) {
        return updateRelationType.update(id, tipoRelacionDto);
    }


    @DeleteMapping("/{id}")
    public void deleteTipoRelacion(@PathVariable("id") Integer id, HttpServletRequest httpServletRequest) {
        deleteRelationType.deleteById(id);
    }

    @GetMapping("/{id}")
    public RelationTypeDto getTipoRelacionById(@PathVariable("id") Integer id) {
        Optional<RelationTypeDto> result = findRelationTypeById.findById(id);
        return result.orElse(null);
    }

    @GetMapping
    public List<RelationTypeDto> getAllTipoRelacion() {
        return findRelationTypes.findAll();
    }






}
