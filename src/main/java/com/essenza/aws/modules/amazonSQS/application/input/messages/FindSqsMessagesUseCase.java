package com.essenza.aws.modules.amazonSQS.application.input.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindSqsMessagesUseCase {
    Page<SqsMessageDto> findAll(Pageable pageable);
}
