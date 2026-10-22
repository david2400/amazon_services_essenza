package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.topics;

import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsTopicRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.CreateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.UpdateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers.SnsTopicMapper;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SnsTopicRepositoryAdapter implements SnsTopicRepository {

    private final JpaSnsTopicRepository jpaRepository;
    private final SnsTopicMapper mapper;

    @Override
    public SnsTopicDto create(CreateSnsTopicDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SnsTopicDto update(Long id, UpdateSnsTopicDto input) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SNS Topic not found with id " + id));
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
    public Optional<SnsTopicDto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<SnsTopicDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<SnsTopicDto> findByArn(String arn) {
        return jpaRepository.findByArn(arn).map(mapper::toDto);
    }
}
