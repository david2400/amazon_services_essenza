package com.essenza.aws.modules.amazonSNS.application.input.topics;

import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindSnsTopicsUseCase {
    Page<SnsTopicDto> findAll(Pageable pageable);
}
