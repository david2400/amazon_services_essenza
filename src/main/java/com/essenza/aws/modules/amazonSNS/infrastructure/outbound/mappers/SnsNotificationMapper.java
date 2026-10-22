package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns.SnsNotificationEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true), uses = JsonMapConverter.class)
public interface SnsNotificationMapper {

    @Mapping(target = "messageAttributes", source = "messageAttributesJson")
    SnsNotificationDto toDto(SnsNotificationEntity entity);

    List<SnsNotificationDto> toDtoList(List<SnsNotificationEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "messageAttributesJson", source = "messageAttributes")
    SnsNotificationEntity toEntity(SnsNotificationDto dto);
}
