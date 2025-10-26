package com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.CreateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.UpdateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.RelationTypeEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface RelationTypeMapper {
    RelationTypeDto toDto(RelationTypeEntity entity);

    List<RelationTypeDto> toDtoList(List<RelationTypeEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RelationTypeEntity toEntity(CreateRelationTypeDto dto);

    List<RelationTypeEntity> toEntityList(List<RelationTypeDto> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateRelationTypeDto dto, @MappingTarget RelationTypeEntity entity);
}
