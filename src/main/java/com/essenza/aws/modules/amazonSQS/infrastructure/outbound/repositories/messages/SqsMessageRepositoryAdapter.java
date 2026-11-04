package com.essenza.aws.modules.amazonSQS.infrastructure.outbound.repositories.messages;

import com.essenza.aws.modules.amazonSQS.application.output.repository.SqsMessageRepository;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;
import com.essenza.aws.modules.amazonSQS.infrastructure.outbound.mappers.SqsMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SqsMessageRepositoryAdapter implements SqsMessageRepository {

    private final JpaSqsMessageRepository jpaRepository;
    private final SqsMessageMapper mapper;

    @Override
    public SqsMessageDto save(SqsMessageDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Page<SqsMessageDto> findByQueueUrl(String queueUrl, Pageable pageable) {
        return jpaRepository.findByQueueUrl(queueUrl, pageable).map(mapper::toDto);
    }

    @Override
    public Page<SqsMessageDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }
}
