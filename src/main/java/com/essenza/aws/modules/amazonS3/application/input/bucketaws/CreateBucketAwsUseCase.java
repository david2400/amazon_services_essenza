package com.essenza.aws.modules.amazonS3.application.input.buketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.CreateBuketAwsDto;

public interface CreateBuketAwsUseCase {
    BuketAwsDto create(CreateBuketAwsDto input);
}
