package com.contract.ai.core.registry;

import com.contract.ai.core.strategy.AiStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI策略注册中心
 * 管理所有AI策略的注册和查找
 */
@Slf4j
@Component
public class AiStrategyRegistry {

    private final Map<String, AiStrategy> strategies = new ConcurrentHashMap<>();

    /**
     * 注册策略
     *
     * @param strategy AI策略
     */
    public void register(AiStrategy strategy) {
        List<String> models = strategy.getModel();
        for (String model : models) {
            if (strategies.containsKey(model)) {
                log.warn("Model [{}] already registered, will be replaced", model);
            }
            strategies.put(model, strategy);
            log.info("Registered AI strategy for model: [{}]", model);
        }
    }

    /**
     * 根据模型名称获取策略
     *
     * @param model 模型名称
     * @return AI策略
     */
    public AiStrategy getStrategy(String model) {
        AiStrategy strategy = strategies.get(model);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for model: " + model);
        }
        return strategy;
    }

    /**
     * 检查模型是否被支持
     *
     * @param model 模型名称
     * @return 是否支持
     */
    public boolean isSupported(String model) {
        return strategies.containsKey(model);
    }

    /**
     * 获取所有支持的模型列表
     *
     * @return 模型列表
     */
    public List<String> getSupportedModels() {
        return List.copyOf(strategies.keySet());
    }

    /**
     * 获取所有注册的策略
     *
     * @return 策略列表
     */
    public List<AiStrategy> getAllStrategies() {
        return List.copyOf(strategies.values());
    }

    /**
     * 移除策略
     *
     * @param model 模型名称
     */
    public void unregister(String model) {
        AiStrategy removed = strategies.remove(model);
        if (removed != null) {
            log.info("Unregistered AI strategy for model: [{}]", model);
        }
    }

    /**
     * 清空所有策略
     */
    public void clear() {
        strategies.clear();
        log.info("Cleared all AI strategies");
    }
}