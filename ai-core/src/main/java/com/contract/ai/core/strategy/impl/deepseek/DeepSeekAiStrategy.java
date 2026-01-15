package com.contract.ai.core.strategy.impl.deepseek;

import com.contract.ai.core.exception.AiErrorCode;
import com.contract.ai.core.exception.AiException;
import com.contract.ai.core.registry.AiStrategyRegistry;
import com.contract.ai.core.strategy.AiStrategy;
import com.contract.ai.feign.convertor.DeepSeekConvertor;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatRequest;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * DeepSeek平台AI策略实现
 * 支持 deepseek-chat 和 deepseek-reasoner 模型
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.strategy.deepseek.enabled", havingValue = "true")
public class DeepSeekAiStrategy implements AiStrategy {

    private final DeepSeekClient deepSeekClient;
    private final AiStrategyRegistry strategyRegistry;
    private final DeepSeekConvertor deepSeekConvertor;

    @Value("${ai.strategy.deepseek.enabled-models:deepseek-chat,deepseek-reasoner}")
    private List<String> enabledModels;

    public DeepSeekAiStrategy(DeepSeekClient deepSeekClient,
                             AiStrategyRegistry strategyRegistry,
                             DeepSeekConvertor deepSeekConvertor) {
        this.deepSeekClient = deepSeekClient;
        this.strategyRegistry = strategyRegistry;
        this.deepSeekConvertor = deepSeekConvertor;
    }

    /**
     * 初始化后自动注册到策略注册中心
     */
    @PostConstruct
    public void registerStrategy() {
        strategyRegistry.register(this);
        log.info("Registered DeepSeek AI strategy for models: {}", enabledModels);
    }

    @Override
    public List<String> getModel() {
        // 返回第一个启用的模型作为默认模型
        return enabledModels.isEmpty() ? Collections.singletonList("deepseek-chat") : enabledModels;
    }

    @Override
    public ChatResponse handleChat(ChatRequest request) {
        log.info("Processing chat request with DeepSeek strategy for model: [{}]", request.getModel());

        try {
            // 使用转换器将标准请求转换为DeepSeek格式
            DeepSeekChatRequest deepSeekRequest = deepSeekConvertor.convertRequest(request);
            log.debug("Converted request to DeepSeek format: model={}, messages={}, responseFormat={}",
                     deepSeekRequest.getModel(),
                     deepSeekRequest.getMessages() != null ? deepSeekRequest.getMessages().size() : 0,
                     deepSeekRequest.getResponseFormat() != null ? deepSeekRequest.getResponseFormat().getType() : "text");

            // 调用DeepSeek平台API
            DeepSeekChatResponse deepSeekResponse = deepSeekClient.chatCompletions(deepSeekRequest);

            // 处理空响应
            if (deepSeekResponse == null) {
                log.warn("DeepSeek API returned null response for model [{}]", request.getModel());
                return null;
            }

            // 使用转换器将DeepSeek响应转换为标准格式
            ChatResponse response = deepSeekConvertor.convertResponse(deepSeekResponse);

            log.info("DeepSeek模型 [{}] 返回回答内容: [{}]", request.getModel(),
                extractResponseContent(response));
            log.debug("Generated response for DeepSeek model [{}]: [{}], usage: {}",
                     request.getModel(), response.getId(), response.getUsage());
            return response;

        } catch (Exception e) {
            log.error("Error calling DeepSeek API for model [{}]: {}", request.getModel(), e.getMessage(), e);

            // 根据异常类型映射到不同的错误码
            AiErrorCode errorCode = mapExceptionToErrorCode(e);
            throw new AiException(errorCode, e);
        }
    }

    @Override
    public boolean supports(String model) {
        return enabledModels.contains(model);
    }

    /**
     * 映射异常到错误码
     */
    private AiErrorCode mapExceptionToErrorCode(Exception e) {
        String errorMessage = e.getMessage().toLowerCase();

        if (errorMessage.contains("400") || errorMessage.contains("bad request")) {
            return AiErrorCode.AI_INVALID_MODEL_PARAMETER;
        } else if (errorMessage.contains("401") || errorMessage.contains("unauthorized") ||
                   errorMessage.contains("api key") || errorMessage.contains("invalid api key")) {
            return AiErrorCode.AI_AUTHENTICATION_FAILED;
        } else if (errorMessage.contains("404") || errorMessage.contains("not found")) {
            return AiErrorCode.AI_MODEL_NOT_SUPPORTED;
        } else if (errorMessage.contains("429") || errorMessage.contains("too many requests") ||
                   errorMessage.contains("rate limit")) {
            return AiErrorCode.AI_REQUEST_LIMIT_EXCEEDED;
        } else if (errorMessage.contains("503") || errorMessage.contains("service unavailable")) {
            return AiErrorCode.AI_SERVICE_UNAVAILABLE;
        } else if (errorMessage.contains("504") || errorMessage.contains("gateway timeout")) {
            return AiErrorCode.AI_REQUEST_TIMEOUT;
        } else if (errorMessage.contains("timeout")) {
            return AiErrorCode.AI_READ_TIMEOUT;
        } else if (errorMessage.contains("insufficient quota") || errorMessage.contains("quota")) {
            return AiErrorCode.AI_QUOTA_EXCEEDED;
        } else if (errorMessage.contains("model") && errorMessage.contains("not found")) {
            return AiErrorCode.AI_MODEL_NOT_SUPPORTED;
        } else {
            return AiErrorCode.AI_SERVICE_ERROR;
        }
    }

    /**
     * 提取响应内容
     *
     * @param response 聊天响应
     * @return 响应内容字符串
     */
    private String extractResponseContent(ChatResponse response) {
        if (response == null || response.getMessages() == null || response.getMessages().isEmpty()) {
            return "无响应内容";
        }

        ChatResponse.Message message = response.getMessages().get(0);
        if (message.getContent() != null && !message.getContent().trim().isEmpty()) {
            // 限制日志内容长度，避免过长
            String content = message.getContent().trim();
            if (content.length() > 500) {
                return content.substring(0, 500) + "...(内容过长，已截断)";
            }
            return content;
        }

        return "无文本内容";
    }
}