package com.essenza.aws.modules.amazonS3.application.input.buketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;

import java.util.Optional;

public interface FindBuketAwsByProjectNameUseCase {
    Optional<BuketAwsDto> findByProjectName(String projectName);
}
