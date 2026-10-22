package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.subscriptions;

import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsSubscriptionRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers.SnsSubscriptionMapper;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SnsSubscriptionRepositoryAdapter implements SnsSubscriptionRepository {

    private final JpaSnsSubscriptionRepository jpaRepository;
    private final SnsSubscriptionMapper mapper;

    @Override
    public SnsSubscriptionDto create(CreateSnsSubscriptionDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SnsSubscriptionDto update(Long id, UpdateSnsSubscriptionDto input) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SNS Subscription not found with id " + id));
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
    public Optional<SnsSubscriptionDto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<SnsSubscriptionDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<SnsSubscriptionDto> findBySubscriptionArn(String subscriptionArn) {
        return jpaRepository.findBySubscriptionArn(subscriptionArn).map(mapper::toDto);
    }

    @Override
    public Page<SnsSubscriptionDto> findByTopicArn(String topicArn, Pageable pageable) {
        return jpaRepository.findByTopicArn(topicArn, pageable).map(mapper::toDto);
    }
}
