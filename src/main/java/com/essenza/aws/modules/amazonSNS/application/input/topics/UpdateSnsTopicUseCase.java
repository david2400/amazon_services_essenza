package com.essenza.aws.modules.amazonSNS.application.input.topics;

import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.UpdateSnsTopicDto;

public interface UpdateSnsTopicUseCase {
    SnsTopicDto update(Long id, UpdateSnsTopicDto input);
}
