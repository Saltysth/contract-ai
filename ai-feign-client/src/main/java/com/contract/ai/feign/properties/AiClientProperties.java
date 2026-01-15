package com.contract.ai.feign.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI客户端属性配置
 * 提供属性配置占位
 */
@Data
@ConfigurationProperties(prefix = "ai.client")
public class AiClientProperties {

    /**
     * 是否启用AI客户端
     */
    private boolean enabled = true;

    /**
     * 服务名称
     */
    private String serviceName = "contract-ai-service";

    /**
     * 基础路径
     */
    private String basePath = "/api/ai";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 60000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 60000;

    /**
     * 重试次数
     */
    private int retryCount = 3;

    /**
     * 重试初始间隔（毫秒）
     */
    private long retryInitialInterval = 100;

    /**
     * 重试最大间隔（毫秒）
     */
    private long retryMaxInterval = 1000;

    /**
     * 日志级别
     */
    private String loggerLevel = "basic";
}