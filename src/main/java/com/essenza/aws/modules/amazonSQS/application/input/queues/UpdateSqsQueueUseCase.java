package com.essenza.aws.modules.amazonSQS.application.input.queues;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.UpdateSqsQueueDto;

public interface UpdateSqsQueueUseCase {
    SqsQueueDto update(Long id, UpdateSqsQueueDto input);
}
