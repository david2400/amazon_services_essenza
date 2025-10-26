package com.essenza.aws.modules.amazonS3.application.input.buketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;

import java.util.Optional;

public interface FindBuketAwsByIdUseCase {
    Optional<BuketAwsDto> findById(Long id);
}
