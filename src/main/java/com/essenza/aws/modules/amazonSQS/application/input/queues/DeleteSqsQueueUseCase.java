package com.essenza.aws.modules.amazonSQS.application.input.queues;

public interface DeleteSqsQueueUseCase {
    boolean delete(Long id);
}
