package com.essenza.aws.modules.amazonSNS.application.output.repository;

import com.essenza.aws.modules.amazonSNS.domain.dto.topics.CreateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.UpdateSnsTopicDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SnsTopicRepository {
    SnsTopicDto create(CreateSnsTopicDto input);

    SnsTopicDto update(Long id, UpdateSnsTopicDto input);

    boolean deleteById(Long id);

    Optional<SnsTopicDto> findById(Long id);

    Page<SnsTopicDto> findAll(Pageable pageable);

    Optional<SnsTopicDto> findByArn(String arn);
}
