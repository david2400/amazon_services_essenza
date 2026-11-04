package com.essenza.aws.modules.amazonSQS.application.input.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SendSqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;

public interface SendSqsMessageUseCase {
    SqsMessageDto send(SendSqsMessageDto input);
}
