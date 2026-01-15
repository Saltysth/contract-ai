package com.contract.ai.core.strategy.impl.iflow;

import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.okhttp.OkHttpClient;
import okhttp3.ConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.concurrent.TimeUnit;

/**
 * 心流平台专用配置
 * 避免与全局Feign配置冲突
 */
@ConditionalOnProperty(name = "ai.strategy.iflow.enabled", havingValue = "true")
public class IflowConfiguration {

    @Value("${ai.strategy.iflow.connect-timeout:60000}")
    private int connectTimeout;

    @Value("${ai.strategy.iflow.read-timeout:120000}")
    private int readTimeout;

    @Value("${ai.strategy.iflow.retry-attempts:3}")
    private int retryAttempts;

    @Value("${ai.strategy.iflow.retry-delay:1000}")
    private long retryDelay;

    @Value("${ai.strategy.iflow.api-key:}")
    private String apiKey;

    /**
     * 心流平台Bearer Token认证拦截器
     */
    @Bean("iflowBearerTokenInterceptor")
    public IflowBearerTokenInterceptor iflowBearerTokenInterceptor() {
        return new IflowBearerTokenInterceptor(apiKey);
    }

    /**
     * 心流平台请求头拦截器
     * 确保请求正确的Content-Type和Accept头
     */
    @Bean("iflowHeaderInterceptor")
    public RequestInterceptor iflowHeaderInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(feign.RequestTemplate template) {
                // 确保Accept头指定期望JSON响应
                template.header("Accept", MediaType.APPLICATION_JSON_VALUE);
                template.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            }
        };
    }

    /**
     * 心流平台专用OkHttp客户端
     */
    @Bean("iflowOkHttpClient")
    public OkHttpClient feignOkHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(
            5, // 最大空闲连接数
            5, TimeUnit.MINUTES // 保持时间
        );

        okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
            .connectionPool(connectionPool)
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build();

        return new OkHttpClient(okHttpClient);
    }
}