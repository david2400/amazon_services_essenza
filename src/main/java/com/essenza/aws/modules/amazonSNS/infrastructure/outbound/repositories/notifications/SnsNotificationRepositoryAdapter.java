package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.notifications;

import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsNotificationRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers.SnsNotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SnsNotificationRepositoryAdapter implements SnsNotificationRepository {

    private final JpaSnsNotificationRepository jpaRepository;
    private final SnsNotificationMapper mapper;

    @Override
    public SnsNotificationDto save(SnsNotificationDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Page<SnsNotificationDto> findByTopicArn(String topicArn, Pageable pageable) {
        return jpaRepository.findByTopicArn(topicArn, pageable).map(mapper::toDto);
    }

    @Override
    public Page<SnsNotificationDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }
}
