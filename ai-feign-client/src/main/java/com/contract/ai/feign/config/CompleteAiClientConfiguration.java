package com.contract.ai.feign.config;

import com.contract.ai.feign.client.AiClient;
import com.contract.ai.feign.client.AiClientConfiguration;
import com.contract.ai.feign.properties.AiClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 完整的AI客户端配置类
 *
 * 包含：
 * 1. Feign客户端启用 (@EnableFeignClients)
 * 2. 配置属性绑定 (@EnableConfigurationProperties)
 * 3. Feign具体配置 (AiClientConfiguration)
 *
 * 使用方式：
 * @SpringBootApplication
 * @Import(CompleteAiClientConfiguration.class)
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 */
@Configuration
@EnableConfigurationProperties(AiClientProperties.class)
@Import({AiClientConfiguration.class})
@EnableFeignClients(clients = AiClient.class)
public class CompleteAiClientConfiguration {

}