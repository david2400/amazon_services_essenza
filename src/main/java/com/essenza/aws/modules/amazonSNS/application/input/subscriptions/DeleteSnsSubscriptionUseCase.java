package com.essenza.aws.modules.amazonSNS.application.input.subscriptions;

public interface DeleteSnsSubscriptionUseCase {
    boolean unsubscribe(Long id);
}
