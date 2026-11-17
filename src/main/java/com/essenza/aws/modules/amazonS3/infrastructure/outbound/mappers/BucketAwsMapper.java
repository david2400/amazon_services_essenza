package com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.CreateBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.UpdateBucketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BucketAwsEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BucketAwsMapper {

    BucketAwsDto toDto(BucketAwsEntity entity);

    List<BucketAwsDto> toDtoList(List<BucketAwsEntity> entities);

//    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BucketAwsEntity toEntity(CreateBucketAwsDto dto);

//    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BucketAwsEntity toEntity(BucketAwsDto dto);

    List<BucketAwsEntity> toEntityList(List<BucketAwsDto> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateBucketAwsDto dto, @MappingTarget BucketAwsEntity entity);
}
