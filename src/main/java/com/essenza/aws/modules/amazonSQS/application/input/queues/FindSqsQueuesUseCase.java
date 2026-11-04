package com.essenza.aws.modules.amazonSQS.application.input.queues;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindSqsQueuesUseCase {
    Page<SqsQueueDto> findAll(Pageable pageable);
}
