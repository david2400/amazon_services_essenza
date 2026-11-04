package com.essenza.aws.modules.amazonSQS.application.input.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.messages.DeleteSqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;

public interface DeleteSqsMessageUseCase {
    SqsMessageDto delete(DeleteSqsMessageDto input);
}
