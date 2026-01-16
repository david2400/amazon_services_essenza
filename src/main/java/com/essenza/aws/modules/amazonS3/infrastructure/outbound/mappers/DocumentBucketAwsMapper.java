package com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers;

import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentBucketAwsEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface DocumentBucketAwsMapper {
    DocumentBucketAwsDto toDto(DocumentBucketAwsEntity entity);

    List<DocumentBucketAwsDto> toDtoList(List<DocumentBucketAwsEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DocumentBucketAwsEntity toEntity(CreateDocumentBucketAwsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usrCrea", ignore = true)
    @Mapping(target = "usrMod", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateDocumentBucketAwsDto dto, @MappingTarget DocumentBucketAwsEntity entity);
}
