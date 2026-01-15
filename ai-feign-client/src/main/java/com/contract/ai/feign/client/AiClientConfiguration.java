package com.contract.ai.feign.client;

import feign.Request;
import feign.Retryer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Feign客户端配置
 * 默认60s超时 + 3次指数退避重试
 *
 * 注意：这个类是Feign的具体配置，不是Spring的自动配置类
 * 实际的Spring配置请参考 com.contract.ai.feign.config.AiClientConfiguration
 */
@Configuration
public class AiClientConfiguration {

    /**
     * 配置请求超时时间
     * 默认60s连接超时，300s读取超时（处理大型PDF文件需要更长时间）
     */
    @Bean("aiClientRequestOptions")
    public Request.Options requestOptions() {
        return new Request.Options(
            (int) TimeUnit.SECONDS.toMillis(60),  // connectTimeout
            (int) TimeUnit.SECONDS.toMillis(300)  // readTimeout - 5分钟用于处理AI视觉任务
        );
    }

    /**
     * 配置重试策略
     * 默认3次重试，指数退避
     */
    @Bean("aiClientRetryer")
    @ConditionalOnMissingBean(Retryer.class)
    public Retryer retryer() {
        // 初始间隔100ms，最大间隔1s，最大重试次数3次
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }
}