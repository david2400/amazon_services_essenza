package com.essenza.aws.modules.amazonS3.domain.dto.buketaws;

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
public class UpdateBuketAwsDto extends CreateBuketAwsDto {
    @NotNull
    @Positive
    private Long id;
}
