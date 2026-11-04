package com.essenza.aws.modules.amazonSQS.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers.JsonMapConverter;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;
import com.essenza.aws.modules.amazonSQS.infrastructure.outbound.persistence.mysql.sqs.SqsMessageEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true), uses = JsonMapConverter.class)
public interface SqsMessageMapper {

    @Mapping(target = "messageAttributes", source = "messageAttributesJson")
    SqsMessageDto toDto(SqsMessageEntity entity);

    List<SqsMessageDto> toDtoList(List<SqsMessageEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "messageAttributesJson", source = "messageAttributes")
    SqsMessageEntity toEntity(SqsMessageDto dto);
}
