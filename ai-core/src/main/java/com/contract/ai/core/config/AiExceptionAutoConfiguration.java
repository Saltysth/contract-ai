package com.contract.ai.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * AI异常处理自动配置
 * 导入统一的异常处理器自动配置
 */
@Configuration
@Import({
    com.contractreview.exception.config.ExceptionAutoConfiguration.class
})
public class AiExceptionAutoConfiguration {

    /**
     * 启用AI异常处理配置
     * 统一的异常处理将通过ExceptionAutoConfiguration自动配置
     */
    public AiExceptionAutoConfiguration() {
        // 构造函数，用于启用配置
    }
}