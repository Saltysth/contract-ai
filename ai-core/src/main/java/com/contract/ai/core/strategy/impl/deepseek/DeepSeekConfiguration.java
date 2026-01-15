package com.contract.ai.core.strategy.impl.deepseek;

import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * DeepSeek平台Feign客户端配置
 */
public class DeepSeekConfiguration {

    /**
     * DeepSeek API 认证拦截器
     * 在请求头中添加 Authorization: Bearer {api_key}
     *
     * @param apiKey DeepSeek API密钥
     * @return 认证拦截器
     */
    @Bean("deepSeekBearerTokenInterceptor")
    public RequestInterceptor deepSeekBearerTokenInterceptor(@Value("${ai.strategy.deepseek.api-key:}") String apiKey) {
        return requestTemplate -> {
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                requestTemplate.header("Authorization", "Bearer " + apiKey.trim());
            }
        };
    }

    /**
     * DeepSeek 请求拦截器 - 设置通用请求头
     *
     * @return 请求拦截器
     */
    @Bean("deepSeekRequestInterceptor")
    public RequestInterceptor deepSeekRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
        };
    }

    /**
     * DeepSeek Feign客户端专用请求配置
     * 60s连接超时，60s读取超时
     */
    @Bean("deepSeekRequestOptions")
    public Request.Options deepSeekRequestOptions() {
        return new Request.Options(
                60, TimeUnit.SECONDS,  // 连接超时
                60, TimeUnit.SECONDS,  // 读取超时
                true                    // 跟随重定向
        );
    }

    /**
     * DeepSeek Feign客户端专用重试策略
     * 3次重试，指数退避
     */
    @Bean("deepSeekRetryer")
    @ConditionalOnMissingBean(Retryer.class)
    public Retryer deepSeekRetryer() {
        // 初始间隔100ms，最大间隔1s，最大重试次数3次
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }
}