package com.contract.ai.core.config;

import com.contract.ai.core.registry.AiStrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 策略自动配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StrategyAutoConfiguration {

    private final AiStrategyRegistry strategyRegistry;

    /**
     * 策略加载器
     * 从配置文件动态加载策略
     */
    @Bean
    @ConditionalOnProperty(name = "ai.strategy.auto-load.enabled", havingValue = "true")
    public StrategyLoader strategyLoader() {
        return new StrategyLoader(strategyRegistry);
    }

    /**
     * 策略加载器
     * 动态加载策略
     */
    public static class StrategyLoader {
        private final AiStrategyRegistry registry;

        public StrategyLoader(AiStrategyRegistry registry) {
            this.registry = registry;
            loadStrategies();
        }

        private void loadStrategies() {
            log.info("Loading AI strategies...");

            // 这里可以从配置文件、数据库或其他地方加载策略配置
            // 示例：根据配置创建不同的策略实例

            // 示例：加载OpenAI策略
            // if (isPropertyEnabled("ai.strategy.openai.enabled")) {
            //     OpenAiStrategy openAiStrategy = new OpenAiStrategy(openaiConfig);
            //     registry.register(openAiStrategy);
            // }

            // 示例：加载Azure策略
            // if (isPropertyEnabled("ai.strategy.azure.enabled")) {
            //     AzureOpenAiStrategy azureStrategy = new AzureOpenAiStrategy(azureConfig);
            //     registry.register(azureStrategy);
            // }

            log.info("Strategy loading completed. Total strategies: {}",
                registry.getAllStrategies().size());
        }
    }
}