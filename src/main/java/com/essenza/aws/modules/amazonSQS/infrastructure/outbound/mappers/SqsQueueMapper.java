package com.essenza.aws.modules.amazonSQS.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers.JsonMapConverter;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.CreateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.UpdateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.infrastructure.outbound.persistence.mysql.sqs.SqsQueueEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true), uses = JsonMapConverter.class)
public interface SqsQueueMapper {

    @Mapping(target = "attributes", source = "attributesJson")
    @Mapping(target = "tags", source = "tagsJson")
    SqsQueueDto toDto(SqsQueueEntity entity);

    List<SqsQueueDto> toDtoList(List<SqsQueueEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "attributesJson", source = "attributes")
    @Mapping(target = "tagsJson", source = "tags")
    SqsQueueEntity toEntity(CreateSqsQueueDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "attributesJson", source = "attributes")
    @Mapping(target = "tagsJson", source = "tags")
    void updateEntityFromDto(UpdateSqsQueueDto dto, @MappingTarget SqsQueueEntity entity);
}
