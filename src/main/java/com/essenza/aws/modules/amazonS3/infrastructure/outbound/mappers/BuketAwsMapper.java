package com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.CreateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.UpdateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BuketAwsEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BuketAwsMapper {

    BuketAwsDto toDto(BuketAwsEntity entity);

    List<BuketAwsDto> toDtoList(List<BuketAwsEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BuketAwsEntity toEntity(CreateBuketAwsDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BuketAwsEntity toEntity(BuketAwsDto dto);

    List<BuketAwsEntity> toEntityList(List<BuketAwsDto> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateBuketAwsDto dto, @MappingTarget BuketAwsEntity entity);
}
