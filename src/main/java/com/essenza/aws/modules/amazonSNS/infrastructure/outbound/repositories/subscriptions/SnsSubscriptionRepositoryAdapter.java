package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.subscriptions;

import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsSubscriptionRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers.SnsSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
                .orElseThrow(() -> new RuntimeException("SNS Subscription not found"));
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
    public List<SnsSubscriptionDto> findAll() {
        return mapper.toDtoList(jpaRepository.findAll());
    }

    @Override
    public Optional<SnsSubscriptionDto> findBySubscriptionArn(String subscriptionArn) {
        return jpaRepository.findBySubscriptionArn(subscriptionArn).map(mapper::toDto);
    }

    @Override
    public List<SnsSubscriptionDto> findByTopicArn(String topicArn) {
        return mapper.toDtoList(jpaRepository.findByTopicArn(topicArn));
    }
}
