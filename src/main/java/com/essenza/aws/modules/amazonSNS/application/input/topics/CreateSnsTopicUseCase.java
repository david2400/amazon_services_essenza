package com.essenza.aws.modules.amazonSNS.application.input.topics;

import com.essenza.aws.modules.amazonSNS.domain.dto.topics.CreateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;

public interface CreateSnsTopicUseCase {
    SnsTopicDto create(CreateSnsTopicDto input);
}
