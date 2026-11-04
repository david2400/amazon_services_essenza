package com.essenza.aws.modules.amazonSQS.application.input.queues;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.CreateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;

public interface CreateSqsQueueUseCase {
    SqsQueueDto create(CreateSqsQueueDto input);
}
