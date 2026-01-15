package com.contract.ai.core.config;

import com.contract.ai.core.registry.AiStrategyRegistry;
import com.contract.ai.core.strategy.impl.glm.GlmVisionAiStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * GLM视觉策略后处理器
 * 在Spring容器初始化完成后注册GLM视觉策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.strategy.glm.vision.enabled", havingValue = "true", matchIfMissing = false)
public class GlmVisionStrategyPostProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final AiStrategyRegistry strategyRegistry;
    private final GlmVisionAiStrategy glmVisionAiStrategy;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            // 根容器初始化完成，注册策略
            try {
                strategyRegistry.register(glmVisionAiStrategy);
                log.info("Successfully registered GLM Vision AI strategy");
                log.info("GLM Vision strategy supports models: glm-4.5v, glm-4v-plus-0111, glm-4v-flash, glm-4.1v-thinking-flashx, glm-4.1v-thinking-flash");
            } catch (Exception e) {
                log.error("Failed to register GLM Vision AI strategy", e);
            }
        }
    }
}