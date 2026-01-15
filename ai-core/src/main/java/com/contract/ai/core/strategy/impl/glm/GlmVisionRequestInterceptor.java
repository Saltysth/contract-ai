package com.contract.ai.core.strategy.impl.glm;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * GLM视觉模型请求拦截器
 * 负责添加Authorization认证头
 */
public class GlmVisionRequestInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate template) {
        // 添加内容类型
        template.header("Content-Type", "application/json");
    }
}