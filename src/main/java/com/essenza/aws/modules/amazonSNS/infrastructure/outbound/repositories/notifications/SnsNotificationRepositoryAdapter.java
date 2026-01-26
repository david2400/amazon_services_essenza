package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.notifications;

import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsNotificationRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers.SnsNotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public List<SnsNotificationDto> findByTopicArn(String topicArn) {
        return mapper.toDtoList(jpaRepository.findByTopicArn(topicArn));
    }

    @Override
    public List<SnsNotificationDto> findAll() {
        return mapper.toDtoList(jpaRepository.findAll());
    }
}
