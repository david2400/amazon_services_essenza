package com.essenza.aws.modules.amazonSQS.application.output.repository;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.CreateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.UpdateSqsQueueDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SqsQueueRepository {
    SqsQueueDto create(CreateSqsQueueDto input);

    SqsQueueDto update(Long id, UpdateSqsQueueDto input);

    boolean deleteById(Long id);

    Optional<SqsQueueDto> findById(Long id);

    Page<SqsQueueDto> findAll(Pageable pageable);

    Optional<SqsQueueDto> findByQueueUrl(String queueUrl);
}
