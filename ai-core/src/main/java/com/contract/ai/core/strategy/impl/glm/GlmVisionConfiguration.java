package com.contract.ai.core.strategy.impl.glm;

import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * GLM视觉模型Feign客户端配置
 */
public class GlmVisionConfiguration {

    /**
     * 配置请求参数
     * 连接超时：60秒
     * 读取超时：5分钟（300秒）- 针对长时间处理任务优化
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                60, TimeUnit.SECONDS,   // 连接超时
                300, TimeUnit.SECONDS,  // 读取超时：5分钟
                true                     // 跟随重定向
        );
    }

    /**
     * 请求拦截器，用于添加Authorization头
     */
    @Bean("glmVisionRequestInterceptor")
    public GlmVisionRequestInterceptor requestInterceptor() {
        return new GlmVisionRequestInterceptor();
    }

    /**
     * GLM视觉模型Feign客户端专用重试策略
     * 禁用重试 - 针对长时间处理任务，避免重复5分钟超时
     */
    @Bean("glmVisionRetryer")
    @ConditionalOnMissingBean(Retryer.class)
    public Retryer glmVisionRetryer() {
        // 禁用重试：使用Feign内置的NEVER_RETRY实例
        return Retryer.NEVER_RETRY;
    }
}