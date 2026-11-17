package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.CreateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.UpdateBuketAwsDto;

import java.util.List;
import java.util.Optional;

public interface BuketAwsRepository {

    BuketAwsDto create(CreateBuketAwsDto input);

    BuketAwsDto update(Long id, UpdateBuketAwsDto input);

    boolean deleteById(Long id);

    Optional<BuketAwsDto> findById(Long id);

    List<BuketAwsDto> findAll();

    List<BuketAwsDto> findByApplicationId(Integer applicationId);

    Optional<BuketAwsDto> findByProjectName(String projectName);
}
