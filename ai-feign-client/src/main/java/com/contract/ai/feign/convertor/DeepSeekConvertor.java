package com.contract.ai.feign.convertor;

import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatRequest;
import com.contract.ai.feign.dto.deepseek.DeepSeekChatResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DeepSeek 平台转换器
 * 负责标准 ChatRequest/ChatResponse 与 DeepSeek 特定格式之间的转换
 */
@Component
public class DeepSeekConvertor implements PlatformConvertor<DeepSeekChatRequest, DeepSeekChatResponse> {

    @Override
    public DeepSeekChatRequest convertRequest(ChatRequest chatRequest) {
        if (chatRequest == null) {
            return null;
        }

        // 转换消息列表
        List<DeepSeekChatRequest.Message> messages = chatRequest.getMessages().stream()
                .map(msg -> DeepSeekChatRequest.Message.builder()
                        .role(msg.getRole())
                        .content(msg.getTextContent() != null ? msg.getTextContent() : "")
                        .build())
                .collect(Collectors.toList());

        // 处理响应格式
        DeepSeekChatRequest.ResponseFormat responseFormat = null;
        if (chatRequest.getResponseReformat() != null) {
            String type = chatRequest.getResponseReformat().getType();
            if ("json_object".equals(type)) {
                responseFormat = new DeepSeekChatRequest.ResponseFormat("json_object");
            } else {
                responseFormat = new DeepSeekChatRequest.ResponseFormat("text");
            }
        }

        // 从扩展参数中提取 DeepSeek 特有参数
        Double frequencyPenalty = null;
        Double presencePenalty = null;

        if (chatRequest.getExtensions() != null) {
            frequencyPenalty = (Double) chatRequest.getExtensions().get("frequencyPenalty");
            presencePenalty = (Double) chatRequest.getExtensions().get("presencePenalty");
        }

        return DeepSeekChatRequest.builder()
                .model(chatRequest.getModel())
                .messages(messages)
                .maxTokens(chatRequest.getMaxTokens())
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
                .stream(chatRequest.getStream())
                .stop(chatRequest.getStop())
                .responseFormat(responseFormat)
                .n(chatRequest.getN())
                .frequencyPenalty(frequencyPenalty != null ? frequencyPenalty : 0.0)
                .presencePenalty(presencePenalty != null ? presencePenalty : 0.0)
                .build();
    }

    @Override
    public ChatResponse convertResponse(DeepSeekChatResponse deepSeekResponse) {
        if (deepSeekResponse == null) {
            return null;
        }

        // 转换消息列表
        List<ChatResponse.Message> messages = null;
        if (deepSeekResponse.getChoices() != null && !deepSeekResponse.getChoices().isEmpty()) {
            messages = deepSeekResponse.getChoices().stream()
                    .filter(choice -> choice.getMessage() != null)
                    .map(choice -> {
                        DeepSeekChatResponse.Message sourceMsg = choice.getMessage();
                        return new ChatResponse.Message(sourceMsg.getRole(), sourceMsg.getContent());
                    })
                    .collect(Collectors.toList());
        }

        // 转换使用统计
        ChatResponse.Usage usage = null;
        if (deepSeekResponse.getUsage() != null) {
            DeepSeekChatResponse.Usage sourceUsage = deepSeekResponse.getUsage();
            usage = new ChatResponse.Usage();
            usage.setPromptTokens(sourceUsage.getPromptTokens());
            usage.setCompletionTokens(sourceUsage.getCompletionTokens());
            usage.setTotalTokens(sourceUsage.getTotalTokens());
        }

        // 确定状态 - DeepSeek 通常返回 "completed" 或根据 finish_reason
        String status = "completed";
        if (deepSeekResponse.getChoices() != null && !deepSeekResponse.getChoices().isEmpty()) {
            String finishReason = deepSeekResponse.getChoices().get(0).getFinishReason();
            if ("stop".equals(finishReason)) {
                status = "completed";
            } else if ("length".equals(finishReason)) {
                status = "length_exceeded";
            } else if ("content_filter".equals(finishReason)) {
                status = "filtered";
            }
        }

        // 创建扩展参数，存储 DeepSeek 特有的响应数据
        Map<String, Object> extensions = new HashMap<>();
        if (deepSeekResponse.getSystemFingerprint() != null) {
            extensions.put("systemFingerprint", deepSeekResponse.getSystemFingerprint());
        }
        if (deepSeekResponse.getObject() != null) {
            extensions.put("object", deepSeekResponse.getObject());
        }

        ChatResponse response = new ChatResponse();
        response.setId(deepSeekResponse.getId());
        response.setModel(deepSeekResponse.getModel());
        response.setCreated(deepSeekResponse.getCreated() != null ?
            java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(deepSeekResponse.getCreated()),
                java.time.ZoneId.systemDefault()) : null);
        response.setMessages(messages != null ? messages : Collections.emptyList());
        response.setUsage(usage);
        response.setStatus(status);
        response.setExtensions(extensions.isEmpty() ? null : extensions);

        return response;
    }
}