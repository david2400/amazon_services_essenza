package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.buketaws.CreateBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.DeleteBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsByApplicationIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsByProjectNameUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.FindBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.buketaws.UpdateBuketAwsUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.CreateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.UpdateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.buketaws.BuketAwsRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class BuketAwsServiceImpl implements
        CreateBuketAwsUseCase,
        UpdateBuketAwsUseCase,
        DeleteBuketAwsUseCase,
        FindBuketAwsByIdUseCase,
        FindBuketAwsUseCase,
        FindBuketAwsByApplicationIdUseCase,
        FindBuketAwsByProjectNameUseCase {

    private final BuketAwsRepositoryAdapter repository;

//    public BuketAwsServiceImpl(BuketAwsRepositoryAdapter repository) {
//        this.repository = repository;
//    }

    @Override
    public BuketAwsDto create(CreateBuketAwsDto input) {
        return repository.create(input);
    }

    @Override
    public BuketAwsDto update(Long id, UpdateBuketAwsDto input) {
        return repository.update(id, input);
    }

    @Override
    public boolean deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BuketAwsDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuketAwsDto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuketAwsDto> findByApplicationId(Integer applicationId) {
        return repository.findByApplicationId(applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BuketAwsDto> findByProjectName(String projectName) {
        return repository.findByProjectName(projectName);
    }
}
