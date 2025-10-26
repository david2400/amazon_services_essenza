package com.essenza.aws.modules.amazonS3.application.input.buketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.UpdateBuketAwsDto;

public interface UpdateBuketAwsUseCase {
    BuketAwsDto update(Long id, UpdateBuketAwsDto input);
}
