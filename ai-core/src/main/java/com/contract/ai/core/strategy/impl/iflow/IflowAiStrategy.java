package com.contract.ai.core.strategy.impl.iflow;

import com.contract.ai.core.exception.AiErrorCode;
import com.contract.ai.core.registry.AiStrategyRegistry;
import com.contract.ai.core.strategy.AiStrategy;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.core.exception.AiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 心流平台AI策略实现
 * 支持GLM-4.6和TBStars2-200B-A13B模型
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.strategy.iflow.enabled", havingValue = "true")
public class IflowAiStrategy implements AiStrategy {

    private final IflowClient iflowClient;
    private final AiStrategyRegistry strategyRegistry;

    @Value("${ai.strategy.iflow.enabled-models:GLM-4.6,TBStars2-200B-A13B}")
    private List<String> enabledModels;

    public IflowAiStrategy(IflowClient iflowClient, AiStrategyRegistry strategyRegistry) {
        this.iflowClient = iflowClient;
        this.strategyRegistry = strategyRegistry;
    }

    /**
     * 初始化后自动注册到策略注册中心
     */
    @PostConstruct
    public void registerStrategy() {
        strategyRegistry.register(this);
        log.info("Registered Iflow AI strategy for models: {}", enabledModels);
    }

    @Override
    public List<String> getModel() {
        // 返回第一个启用的模型作为默认模型
        return enabledModels.isEmpty() ? Collections.singletonList("GLM-4.6") : enabledModels;
    }

    @Override
    public ChatResponse handleChat(ChatRequest request) {
        log.info("Processing chat request with Iflow strategy for model: [{}]", request.getModel());

        try {
            // 构建心流平台请求
            IflowRequest iflowRequest = buildIflowRequest(request);

            // 调用心流平台API
            IflowResponse iflowResponse = iflowClient.chatCompletions(iflowRequest);

            // 转换响应格式
            ChatResponse response = convertToChatResponse(iflowResponse, request.getModel());

            log.info("心流模型 [{}] 返回回答内容: [{}]", request.getModel(),
                extractResponseContent(response));
            log.debug("Generated response for Iflow model [{}]: [{}]", request.getModel(), response.getId());
            return response;

        } catch (Exception e) {
            log.error("Error calling Iflow API for model [{}]: {}", request.getModel(), e.getMessage(), e);

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
     * 构建心流平台请求
     */
    private IflowRequest buildIflowRequest(ChatRequest request) {
        IflowRequest iflowRequest = new IflowRequest();
        iflowRequest.setModel(request.getModel());

        // 转换消息格式
        List<IflowRequest.Message> messages = request.getMessages().stream()
            .map(msg -> {
                IflowRequest.Message message = new IflowRequest.Message();
                message.setRole(msg.getRole());
                message.setContent(msg.getTextContent() != null ? msg.getTextContent() : "");
                return message;
            })
            .collect(Collectors.toList());
        iflowRequest.setMessages(messages);

        // 设置可选参数
        iflowRequest.setMaxTokens(request.getMaxTokens());
        iflowRequest.setTemperature(request.getTemperature());
        iflowRequest.setTopP(request.getTopP());
        // 心流平台：强制设置为非流式响应，避免返回text/event-stream
        iflowRequest.setStream(false);
        iflowRequest.setStop(request.getStop());

        return iflowRequest;
    }

    /**
     * 转换心流平台响应为标准ChatResponse
     */
    private ChatResponse convertToChatResponse(IflowResponse iflowResponse, String model) {
        ChatResponse response = new ChatResponse();
        response.setId(iflowResponse.getId());
        response.setModel(model);

        // 转换时间戳
        if (iflowResponse.getCreated() != null) {
            response.setCreated(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(iflowResponse.getCreated()),
                ZoneId.systemDefault()
            ));
        } else {
            response.setCreated(LocalDateTime.now());
        }

        response.setStatus("success");

        // 转换消息
        if (iflowResponse.getChoices() != null && !iflowResponse.getChoices().isEmpty()) {
            IflowResponse.Choice choice = iflowResponse.getChoices().get(0);
            if (choice.getMessage() != null) {
                ChatResponse.Message message = new ChatResponse.Message();
                message.setRole(choice.getMessage().getRole());
                message.setContent(choice.getMessage().getContent());
                response.setMessages(Collections.singletonList(message));
            } else {
                response.setMessages(Collections.emptyList());
            }
        } else {
            response.setMessages(Collections.emptyList());
        }

        // 转换使用统计
        if (iflowResponse.getUsage() != null) {
            ChatResponse.Usage usage = new ChatResponse.Usage();
            usage.setPromptTokens(iflowResponse.getUsage().getPromptTokens());
            usage.setCompletionTokens(iflowResponse.getUsage().getCompletionTokens());
            usage.setTotalTokens(iflowResponse.getUsage().getTotalTokens());
            response.setUsage(usage);
        }

        return response;
    }

    /**
     * 映射异常到错误码
     */
    private AiErrorCode mapExceptionToErrorCode(Exception e) {
        String errorMessage = e.getMessage().toLowerCase();

        if (errorMessage.contains("400") || errorMessage.contains("bad request")) {
            return AiErrorCode.AI_INVALID_MODEL_PARAMETER;
        } else if (errorMessage.contains("401") || errorMessage.contains("unauthorized") ||
            errorMessage.contains("api key")) {
            return AiErrorCode.AI_AUTHENTICATION_FAILED;
        } else if (errorMessage.contains("404") || errorMessage.contains("not found")) {
            return AiErrorCode.AI_MODEL_NOT_SUPPORTED;
        } else if (errorMessage.contains("429") || errorMessage.contains("too many requests")) {
            return AiErrorCode.AI_REQUEST_LIMIT_EXCEEDED;
        } else if (errorMessage.contains("503") || errorMessage.contains("service unavailable")) {
            return AiErrorCode.AI_SERVICE_UNAVAILABLE;
        } else if (errorMessage.contains("504") || errorMessage.contains("gateway timeout")) {
            return AiErrorCode.AI_REQUEST_TIMEOUT;
        } else if (errorMessage.contains("timeout")) {
            return AiErrorCode.AI_READ_TIMEOUT;
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