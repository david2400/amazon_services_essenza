package com.essenza.aws.modules.amazonSQS.application.output.repository;

import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SqsMessageRepository {
    SqsMessageDto save(SqsMessageDto input);

    Page<SqsMessageDto> findByQueueUrl(String queueUrl, Pageable pageable);

    Page<SqsMessageDto> findAll(Pageable pageable);
}
