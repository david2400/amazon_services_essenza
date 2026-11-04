package com.essenza.aws.modules.amazonSQS.application.input.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindSqsMessagesByQueueUseCase {
    Page<SqsMessageDto> findByQueueUrl(String queueUrl, Pageable pageable);
}
