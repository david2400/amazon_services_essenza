package com.essenza.aws.modules.amazonSQS.infrastructure.outbound.repositories.queues;

import com.essenza.aws.modules.amazonSQS.application.output.repository.SqsQueueRepository;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.CreateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.UpdateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.infrastructure.outbound.mappers.SqsQueueMapper;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqsQueueRepositoryAdapter implements SqsQueueRepository {

    private final JpaSqsQueueRepository jpaRepository;
    private final SqsQueueMapper mapper;

    @Override
    public SqsQueueDto create(CreateSqsQueueDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SqsQueueDto update(Long id, UpdateSqsQueueDto input) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SQS Queue not found with id " + id));
        mapper.updateEntityFromDto(input, entity);
        var updated = jpaRepository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    public boolean deleteById(Long id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<SqsQueueDto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<SqsQueueDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<SqsQueueDto> findByQueueUrl(String queueUrl) {
        return jpaRepository.findByQueueUrl(queueUrl).map(mapper::toDto);
    }
}
