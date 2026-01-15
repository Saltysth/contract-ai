package com.contract.ai.core.strategy.impl.iflow;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 心流平台Bearer Token拦截器
 */
public class IflowBearerTokenInterceptor implements RequestInterceptor {

    private final String apiKey;

    public IflowBearerTokenInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + apiKey);
    }
}