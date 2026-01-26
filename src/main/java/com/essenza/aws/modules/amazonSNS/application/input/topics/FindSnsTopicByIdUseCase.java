package com.essenza.aws.modules.amazonSNS.application.input.topics;

import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;

import java.util.Optional;

public interface FindSnsTopicByIdUseCase {
    Optional<SnsTopicDto> findById(Long id);
}
