package com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFilesAmazonS3Dto extends CreateFilesAmazonS3Dto {
    @NotNull
    @Positive
    private Long id;
}
