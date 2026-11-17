package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.bucketaws.CreateBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.DeleteBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsByApplicationIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsByProjectNameUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.FindBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.bucketaws.UpdateBucketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.output.repository.BucketAwsRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.CreateBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.UpdateBucketAwsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class BucketAwsServiceImpl implements
        CreateBucketAwsUseCase,
        UpdateBucketAwsUseCase,
        DeleteBucketAwsUseCase,
        FindBucketAwsByIdUseCase,
        FindBucketAwsUseCase,
        FindBucketAwsByApplicationIdUseCase,
        FindBucketAwsByProjectNameUseCase {

    private final BucketAwsRepository repository;

    @Override
    public BucketAwsDto create(CreateBucketAwsDto input) {
        return repository.create(input);
    }

    @Override
    public BucketAwsDto update(Long id, UpdateBucketAwsDto input) {
        return repository.update(id, input);
    }

    @Override
    public boolean deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BucketAwsDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BucketAwsDto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BucketAwsDto> findByApplicationId(Integer applicationId, Pageable pageable) {
        return repository.findByApplicationId(applicationId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BucketAwsDto> findByProjectName(String projectName) {
        return repository.findByProjectName(projectName);
    }
}
