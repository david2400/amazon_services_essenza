package com.essenza.aws.modules.amazonSQS.application.input.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SendSqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;

import java.util.List;

public interface SendSqsMessageBatchUseCase {
    List<SqsMessageDto> sendBatch(List<SendSqsMessageDto> inputs);
}
