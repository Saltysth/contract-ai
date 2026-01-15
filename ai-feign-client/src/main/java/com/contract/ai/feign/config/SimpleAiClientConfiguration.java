package com.contract.ai.feign.config;

import org.springframework.context.annotation.Configuration;

/**
 * 最简单的AI客户端配置（占位符）
 * 不启用Feign客户端，仅作为配置示例
 *
 * 注意：如需启用Feign客户端，请使用 CompleteAiClientConfiguration
 * 或在应用主类上添加 @EnableFeignClients(clients = AiClient.class)
 *
 * 适用于：
 * 1. 作为配置示例展示
 * 2. 需要完全自定义配置的场景（手动启用Feign）
 */
@Configuration
public class SimpleAiClientConfiguration {

}