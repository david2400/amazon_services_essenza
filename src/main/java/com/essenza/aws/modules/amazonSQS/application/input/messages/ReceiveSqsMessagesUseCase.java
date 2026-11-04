package com.essenza.aws.modules.amazonSQS.application.input.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.messages.ReceiveSqsMessagesDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;

import java.util.List;

public interface ReceiveSqsMessagesUseCase {
    List<SqsMessageDto> receive(ReceiveSqsMessagesDto input);
}
