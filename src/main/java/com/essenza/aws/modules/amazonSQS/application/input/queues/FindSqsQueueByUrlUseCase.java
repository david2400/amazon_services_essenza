package com.essenza.aws.modules.amazonSQS.application.input.queues;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;

import java.util.Optional;

public interface FindSqsQueueByUrlUseCase {
    Optional<SqsQueueDto> findByQueueUrl(String queueUrl);
}
