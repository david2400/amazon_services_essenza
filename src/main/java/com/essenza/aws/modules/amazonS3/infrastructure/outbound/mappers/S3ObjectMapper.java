package com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonS3.domain.dto.s3object.S3ObjectDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.S3ObjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Builder;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface S3ObjectMapper {

    S3ObjectDto toDto(S3ObjectEntity entity);

    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    S3ObjectEntity toEntity(S3ObjectDto dto);
}
