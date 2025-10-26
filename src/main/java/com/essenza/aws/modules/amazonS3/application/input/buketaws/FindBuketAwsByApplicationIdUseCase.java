package com.essenza.aws.modules.amazonS3.application.input.buketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;

import java.util.List;

public interface FindBuketAwsByApplicationIdUseCase {
    List<BuketAwsDto> findByApplicationId(Integer applicationId);
}
