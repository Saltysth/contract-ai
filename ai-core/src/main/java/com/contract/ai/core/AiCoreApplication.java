package com.contract.ai.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * AI服务应用启动类
 * 服务名: contract-ai-service
 */
@SpringBootApplication(scanBasePackages = "com.contract.ai", exclude = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableFeignClients(clients = {
    com.contract.ai.core.strategy.impl.deepseek.DeepSeekClient.class,
    com.contract.ai.core.strategy.impl.glm.GlmVisionClient.class,
    com.contract.ai.core.strategy.impl.iflow.IflowClient.class
})
@Import({
    com.contract.ai.core.config.AiExceptionAutoConfiguration.class,
    com.contract.ai.core.config.StrategyAutoConfiguration.class
})
public class AiCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCoreApplication.class, args);
    }
}