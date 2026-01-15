package com.contract.ai.core.router;

import com.contract.ai.core.registry.AiStrategyRegistry;
import com.contract.ai.core.strategy.AiStrategy;
import com.contract.ai.core.strategy.VisionAiStrategy;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI路由器
 * 根据模型名称路由到对应的AI策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiRouter {

    private final AiStrategyRegistry strategyRegistry;

    /**
     * 路由聊天请求到对应的AI策略
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse route(ChatRequest request) {
        String model = request.getModel();

        log.debug("Routing chat request for model: [{}]", model);

        // 检查模型是否被支持
        if (!strategyRegistry.isSupported(model)) {
            throw new IllegalArgumentException("Unsupported model: " + model +
                ". Supported models: " + strategyRegistry.getSupportedModels());
        }

        // 获取对应的策略并处理请求
        AiStrategy strategy = strategyRegistry.getStrategy(model);
        log.debug("Found strategy [{}] for model: [{}]", strategy.getClass().getSimpleName(), model);

        try {
            ChatResponse response = strategy.handleChat(request);
            log.debug("Successfully processed chat request for model: [{}]", model);
            return response;
        } catch (Exception e) {
            log.error("Error processing chat request for model: [{}]", model, e);
            throw e;
        }
    }

    /**
     * 路由带文件的视觉聊天请求到对应的AI策略
     *
     * @param request 聊天请求
     * @param files 上传的文件列表
     * @return 聊天响应
     */
    public ChatResponse routeWithVision(ChatRequest request, MultipartFile[] files) {
        String model = request.getModel();

        log.debug("Routing vision chat request for model: [{}] with {} files", model, files != null ? files.length : 0);

        // 检查模型是否被支持
        if (!strategyRegistry.isSupported(model)) {
            throw new IllegalArgumentException("Unsupported model: " + model +
                ". Supported models: " + strategyRegistry.getSupportedModels());
        }

        // 获取对应的策略
        AiStrategy strategy = strategyRegistry.getStrategy(model);
        log.debug("Found strategy [{}] for model: [{}]", strategy.getClass().getSimpleName(), model);

        // 检查策略是否支持视觉功能
        if (!(strategy instanceof VisionAiStrategy)) {
            throw new IllegalArgumentException("Model " + model + " does not support vision features");
        }

        try {
            VisionAiStrategy visionStrategy = (VisionAiStrategy) strategy;
            ChatResponse response = visionStrategy.handleChatWithVision(request, files);
            log.debug("Successfully processed vision chat request for model: [{}]", model);
            return response;
        } catch (Exception e) {
            log.error("Error processing vision chat request for model: [{}]", model, e);
            throw e;
        }
    }

    /**
     * 路由带base64图片的视觉聊天请求到对应的AI策略
     *
     * @param request 聊天请求
     * @param imageMap 图片文件名到base64数据的映射
     * @return 聊天响应
     */
    public ChatResponse routeWithVision(ChatRequest request, java.util.Map<String, String> imageMap) {
        String model = request.getModel();

        log.debug("Routing vision chat request for model: [{}] with {} base64 images", model, imageMap != null ? imageMap.size() : 0);

        // 检查模型是否被支持
        if (!strategyRegistry.isSupported(model)) {
            throw new IllegalArgumentException("Unsupported model: " + model +
                ". Supported models: " + strategyRegistry.getSupportedModels());
        }

        // 获取对应的策略
        AiStrategy strategy = strategyRegistry.getStrategy(model);
        log.debug("Found strategy [{}] for model: [{}]", strategy.getClass().getSimpleName(), model);

        // 检查策略是否支持视觉功能
        if (!(strategy instanceof VisionAiStrategy)) {
            throw new IllegalArgumentException("Model " + model + " does not support vision features");
        }

        try {
            VisionAiStrategy visionStrategy = (VisionAiStrategy) strategy;
            ChatResponse response = visionStrategy.handleChatWithVision(request, imageMap);
            log.debug("Successfully processed vision chat request with base64 images for model: [{}]", model);
            return response;
        } catch (Exception e) {
            log.error("Error processing vision chat request with base64 images for model: [{}]", model, e);
            throw e;
        }
    }

    /**
     * 路由带文件URL的聊天请求到对应的AI策略
     *
     * @param request 聊天请求
     * @param fileMap 文件名到URL的映射
     * @return 聊天响应
     */
    public ChatResponse routeWithFiles(ChatRequest request, java.util.Map<String, String> fileMap) {
        String model = request.getModel();

        log.debug("Routing file URL chat request for model: [{}] with {} files", model, fileMap != null ? fileMap.size() : 0);

        // 检查模型是否被支持
        if (!strategyRegistry.isSupported(model)) {
            throw new IllegalArgumentException("Unsupported model: " + model +
                ". Supported models: " + strategyRegistry.getSupportedModels());
        }

        // 获取对应的策略
        AiStrategy strategy = strategyRegistry.getStrategy(model);
        log.debug("Found strategy [{}] for model: [{}]", strategy.getClass().getSimpleName(), model);

        // 检查策略是否支持文件处理功能
        if (!(strategy instanceof com.contract.ai.core.strategy.impl.glm.GlmVisionAiStrategy)) {
            throw new IllegalArgumentException("Model " + model + " does not support file URL features");
        }

        try {
            com.contract.ai.core.strategy.impl.glm.GlmVisionAiStrategy fileStrategy =
                (com.contract.ai.core.strategy.impl.glm.GlmVisionAiStrategy) strategy;
            ChatResponse response = fileStrategy.handleChatWithFiles(request, fileMap);
            log.debug("Successfully processed file URL chat request for model: [{}]", model);
            return response;
        } catch (Exception e) {
            log.error("Error processing file URL chat request for model: [{}]", model, e);
            throw e;
        }
    }

    /**
     * 检查模型是否被支持
     *
     * @param model 模型名称
     * @return 是否支持
     */
    public boolean isModelSupported(String model) {
        return strategyRegistry.isSupported(model);
    }

    /**
     * 获取所有支持的模型
     *
     * @return 模型列表
     */
    public java.util.List<String> getSupportedModels() {
        return strategyRegistry.getSupportedModels();
    }
}