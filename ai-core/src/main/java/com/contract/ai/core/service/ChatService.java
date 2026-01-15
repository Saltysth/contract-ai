package com.contract.ai.core.service;

import com.contract.ai.core.exception.AiErrorCode;
import com.contract.ai.core.router.AiRouter;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.util.ImageBase64Validator;
import com.contract.ai.core.exception.AiException;
import com.contractreview.exception.enums.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * 聊天服务
 * 提供参数校验、归一化与异常映射功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiRouter aiRouter;

    /**
     * 处理纯文本聊天请求
     * 仅支持文本内容，不支持图片
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse chatTextOnly(ChatRequest request) {
        try {
            // 参数校验和归一化
            ChatRequest normalizedRequest = validateAndNormalizeTextOnly(request);

            // 纯文本聊天，直接路由
            ChatResponse response = aiRouter.route(normalizedRequest);
            // 应用响应内容清理
            response = cleanChatResponse(response);
            log.info("AI模型 [{}] 返回回答内容: [{}]", normalizedRequest.getModel(),
                extractResponseContent(response));
            return response;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid text-only chat request: {}", e.getMessage());
            throw new AiException(AiErrorCode.AI_MISSING_REQUIRED_PARAMETER, e.getMessage());

        } catch (AiException e) {
            log.error("AI error in text-only chat service: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error in text-only chat service", e);
            throw new AiException(CommonErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 处理聊天请求
     * 支持文本和base64格式图片输入
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse chat(ChatRequest request) {
        try {
            // 参数校验和归一化
            ChatRequest normalizedRequest = validateAndNormalize(request);

            // 检查是否包含图片或文件，决定路由方式
            boolean hasImages = normalizedRequest.getMessages().stream()
                .anyMatch(msg -> msg.isMultimodal() &&
                    msg.getMultimodalContent().stream()
                        .anyMatch(item -> "image_url".equals(item.getType())));

            boolean hasFiles = normalizedRequest.getMessages().stream()
                .anyMatch(msg -> msg.isMultimodal() &&
                    msg.getMultimodalContent().stream()
                        .anyMatch(item -> "file_url".equals(item.getType())));

            ChatResponse response;
            if (hasFiles && !hasImages) {
                // 只包含文件，使用文件处理路由
                Map<String, String> fileMap = extractFileMap(normalizedRequest);
                response = aiRouter.routeWithFiles(normalizedRequest, fileMap);
            } else if (hasImages) {
                // 包含图片，使用视觉模型路由
                Map<String, String> imageMap = extractImageMap(normalizedRequest);
                response = aiRouter.routeWithVision(normalizedRequest, imageMap);
            } else {
                // 纯文本聊天
                response = aiRouter.route(normalizedRequest);
            }
            // 应用响应内容清理
            response = cleanChatResponse(response);
            log.info("AI模型 [{}] 返回回答内容: [{}]", normalizedRequest.getModel(),
                extractResponseContent(response));
            return response;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid chat request: {}", e.getMessage());
            throw new AiException(AiErrorCode.AI_MISSING_REQUIRED_PARAMETER, e.getMessage());

        } catch (AiException e) {
            log.error("AI error in chat service: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error in chat service", e);
            throw new AiException(CommonErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 处理带文件的视觉聊天请求
     *
     * @param request 聊天请求
     * @param files 上传的文件列表
     * @return 聊天响应
     */
    public ChatResponse chatWithVision(ChatRequest request, MultipartFile[] files) {
        try {
            // 参数校验和归一化
            ChatRequest normalizedRequest = validateAndNormalizeVision(request, files);

            // 路由到对应的AI策略
            ChatResponse response = aiRouter.routeWithVision(normalizedRequest, files);
            // 应用响应内容清理
            response = cleanChatResponse(response);
            log.info("AI模型 [{}] 返回回答内容: [{}]", normalizedRequest.getModel(),
                extractResponseContent(response));
            return response;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid vision chat request: {}", e.getMessage());
            throw new AiException(AiErrorCode.AI_MISSING_REQUIRED_PARAMETER, e.getMessage());

        } catch (AiException e) {
            log.error("AI error in vision chat service: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error in vision chat service", e);
            throw new AiException(CommonErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 纯文本参数校验和归一化
     * 仅支持文本内容，不允许图片
     *
     * @param request 原始请求
     * @return 归一化后的请求
     */
    private ChatRequest validateAndNormalizeTextOnly(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("聊天请求不能为空");
        }

        // 校验模型名称
        if (!StringUtils.hasText(request.getModel())) {
            throw new IllegalArgumentException("模型名称不能为空");
        }

        // 校验消息列表
        List<ChatRequest.Message> messages = request.getMessages();
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }

        // 校验每个消息
        for (ChatRequest.Message message : messages) {
            if (message == null) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
            if (!StringUtils.hasText(message.getRole())) {
                throw new IllegalArgumentException("消息角色不能为空");
            }

            // 纯文本聊天：只允许字符串内容或多模态中的文本项
            boolean hasTextContent = StringUtils.hasText(message.getTextContent());
            boolean hasMultimodalContent = message.isMultimodal();

            if (hasTextContent && !hasMultimodalContent) {
                // 纯文本消息，允许
                continue;
            } else if (hasMultimodalContent) {
                // 检查多模态内容中是否只有文本项
                for (ChatRequest.Message.ContentItem item : message.getMultimodalContent()) {
                    if (!"text".equals(item.getType())) {
                        throw new IllegalArgumentException("纯文本聊天接口不支持图片或其他类型内容");
                    }
                }
            } else {
                throw new IllegalArgumentException("消息内容不能为空");
            }

            // 校验角色有效性
            String role = message.getRole().toLowerCase();
            if (!List.of("system", "user", "assistant").contains(role)) {
                throw new IllegalArgumentException("无效的消息角色: " + role);
            }
        }

        // 归一化参数
        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
            .platform(request.getPlatform())
            .model(request.getModel())
            .messages(messages);

        // 设置默认参数
        if (request.getMaxTokens() != null && request.getMaxTokens() > 0) {
            builder.maxTokens(request.getMaxTokens());
        } else {
            builder.maxTokens(4096); // 默认最大token数
        }

        if (request.getTemperature() != null && request.getTemperature() >= 0 && request.getTemperature() <= 2) {
            builder.temperature(request.getTemperature());
        } else {
            builder.temperature(0.7); // 默认温度
        }

        if (request.getTopP() != null && request.getTopP() >= 0 && request.getTopP() <= 1) {
            builder.topP(request.getTopP());
        } else {
            builder.topP(0.7); // 默认topP
        }

        if (request.getStream() != null) {
            builder.stream(request.getStream());
        } else {
            builder.stream(false); // 默认非流式
        }

        builder.stop(request.getStop());

        ChatRequest normalized = builder.build();

        log.debug("Normalized text-only chat request for model: [{}]", normalized.getModel());
        return normalized;
    }

    /**
     * 参数校验和归一化
     * 支持文本和base64图片验证
     *
     * @param request 原始请求
     * @return 归一化后的请求
     */
    private ChatRequest validateAndNormalize(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("聊天请求不能为空");
        }

        // 校验模型名称
        if (!StringUtils.hasText(request.getModel())) {
            throw new IllegalArgumentException("模型名称不能为空");
        }

        // 校验消息列表
        List<ChatRequest.Message> messages = request.getMessages();
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }

        // 校验每个消息
        for (ChatRequest.Message message : messages) {
            if (message == null) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
            if (!StringUtils.hasText(message.getRole())) {
                throw new IllegalArgumentException("消息角色不能为空");
            }

            // 校验内容：文本内容或多模态内容至少需要一个
            boolean hasTextContent = StringUtils.hasText(message.getTextContent());
            boolean hasMultimodalContent = message.isMultimodal();

            if (!hasTextContent && !hasMultimodalContent) {
                throw new IllegalArgumentException("消息内容不能为空");
            }

            // 校验角色有效性
            String role = message.getRole().toLowerCase();
            if (!List.of("system", "user", "assistant").contains(role)) {
                throw new IllegalArgumentException("无效的消息角色: " + role);
            }

            // 验证多模态内容
            if (hasMultimodalContent) {
                validateMultimodalContent(message.getMultimodalContent());
            }
        }

        // 归一化参数
        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
            .platform(request.getPlatform())
            .model(request.getModel())
            .messages(messages);

        // 设置默认参数
        if (request.getMaxTokens() != null && request.getMaxTokens() > 0) {
            builder.maxTokens(request.getMaxTokens());
        } else {
            builder.maxTokens(4096); // 默认最大token数
        }

        if (request.getTemperature() != null && request.getTemperature() >= 0 && request.getTemperature() <= 2) {
            builder.temperature(request.getTemperature());
        } else {
            builder.temperature(0.7); // 默认温度
        }

        if (request.getTopP() != null && request.getTopP() >= 0 && request.getTopP() <= 1) {
            builder.topP(request.getTopP());
        } else {
            builder.topP(0.7); // 默认topP
        }

        if (request.getStream() != null) {
            builder.stream(request.getStream());
        } else {
            builder.stream(false); // 默认非流式
        }

        builder.stop(request.getStop());

        ChatRequest normalized = builder.build();
        boolean hasImages = normalized.getMessages().stream()
            .anyMatch(msg -> msg.isMultimodal() &&
                msg.getMultimodalContent().stream()
                    .anyMatch(item -> "image_url".equals(item.getType())));

        log.debug("Normalized chat request for model: [{}] with images: {}",
            normalized.getModel(), hasImages);
        return normalized;
    }

    /**
     * 验证多模态内容
     */
    private void validateMultimodalContent(List<ChatRequest.Message.ContentItem> contentItems) {
        if (contentItems == null || contentItems.isEmpty()) {
            throw new IllegalArgumentException("多模态内容不能为空");
        }

        for (int i = 0; i < contentItems.size(); i++) {
            ChatRequest.Message.ContentItem item = contentItems.get(i);

            if (item.getType() == null) {
                throw new IllegalArgumentException(String.format("第%d个内容项类型不能为空", i + 1));
            }

            switch (item.getType()) {
                case "text":
                    if (item.getText() == null || item.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException(String.format("第%d个文本内容不能为空", i + 1));
                    }
                    break;

                case "image_url":
                    if (item.getImage_url() == null || item.getImage_url().getUrl() == null
                        || item.getImage_url().getUrl().trim().isEmpty()) {
                        throw new IllegalArgumentException(String.format("第%d个图片URL不能为空", i + 1));
                    }

                    // 验证base64图片格式
                    ImageBase64Validator.ValidationResult validation =
                        ImageBase64Validator.validateBase64Image(item.getImage_url().getUrl());

                    if (!validation.isValid()) {
                        throw new IllegalArgumentException(String.format(
                            "第%d张图片格式验证失败: %s", i + 1, validation.getErrorMessage()));
                    }
                    break;

                case "file_url":
                    if (item.getFile_url() == null || item.getFile_url().getUrl() == null
                        || item.getFile_url().getUrl().trim().isEmpty()) {
                        throw new IllegalArgumentException(String.format("第%d个文件URL不能为空", i + 1));
                    }

                    // 验证文件URL格式（暂时只验证URL格式，不验证具体文件类型）
                    String fileUrl = item.getFile_url().getUrl();
                    if (!isValidUrl(fileUrl)) {
                        throw new IllegalArgumentException(String.format(
                            "第%d个文件URL格式无效: %s", i + 1, fileUrl));
                    }
                    break;

                default:
                    throw new IllegalArgumentException(String.format("不支持的内容类型: %s", item.getType()));
            }
        }
    }

    /**
     * 从请求中提取图片映射
     * @param request 聊天请求
     * @return 图片文件名到base64数据的映射
     */
    private Map<String, String> extractImageMap(ChatRequest request) {
        Map<String, String> imageMap = new HashMap<>();
        int imageIndex = 1;

        for (ChatRequest.Message message : request.getMessages()) {
            if (message.isMultimodal()) {
                for (ChatRequest.Message.ContentItem item : message.getMultimodalContent()) {
                    if ("image_url".equals(item.getType()) && item.getImage_url() != null) {
                        String fileName = "image_" + imageIndex + ".jpg";
                        imageMap.put(fileName, item.getImage_url().getUrl());
                        imageIndex++;
                    }
                }
            }
        }

        return imageMap;
    }

    /**
     * 从请求中提取文件映射
     * @param request 聊天请求
     * @return 文件名到URL的映射
     */
    private Map<String, String> extractFileMap(ChatRequest request) {
        Map<String, String> fileMap = new HashMap<>();
        int fileIndex = 1;

        for (ChatRequest.Message message : request.getMessages()) {
            if (message.isMultimodal()) {
                for (ChatRequest.Message.ContentItem item : message.getMultimodalContent()) {
                    if ("file_url".equals(item.getType()) && item.getFile_url() != null) {
                        String fileName = "file_" + fileIndex;
                        fileMap.put(fileName, item.getFile_url().getUrl());
                        fileIndex++;
                    }
                }
            }
        }

        return fileMap;
    }

    /**
     * 视觉聊天请求参数校验和归一化
     *
     * @param request 原始请求
     * @param files 上传的文件
     * @return 归一化后的请求
     */
    private ChatRequest validateAndNormalizeVision(ChatRequest request, MultipartFile[] files) {
        // 执行基础校验
        ChatRequest normalized = validateAndNormalize(request);

        // 额外的视觉校验
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    // 文件大小校验（GLM视觉模型限制）
                    if (file.getSize() > 200 * 1024 * 1024) { // 200MB
                        throw new IllegalArgumentException("文件大小超过限制，最大支持200MB: " + file.getOriginalFilename());
                    }

                    // 文件名校验
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || originalFilename.trim().isEmpty()) {
                        throw new IllegalArgumentException("文件名不能为空");
                    }
                }
            }
        }

        return normalized;
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

    /**
     * 验证URL格式
     *
     * @param url URL字符串
     * @return 是否为有效URL
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            // 验证是否为有效的HTTP/HTTPS URL
            new java.net.URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }

    /**
     * 清理所有模型返回结果中的特殊字符
     * 移除模型返回中可能包含的干扰字符
     * 应用到所有AI模型的响应结果
     *
     * @param content 原始响应内容
     * @return 清理后的响应内容
     */
    private String cleanResponseContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        // 移除所有模型可能返回的特殊字符
        String cleaned = content;

        // 移除代码块标记（支持json、html、markdown等）
        cleaned = cleaned.replaceAll("^```(?:json|html|markdown|xml|yaml|sql|javascript|typescript|python|java|go|rust|php|c|cpp|csharp|shell|bash|powershell|docker|diff|log|text|txt)?\\s*\\n?\\s*", "");
        cleaned = cleaned.replaceAll("\\s*\\n?\\s*```$", "");

        // 移除常见的控制字符和特殊符号
        cleaned = cleaned.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", ""); // 控制字符
        cleaned = cleaned.replaceAll("[\\uFFFD]", ""); // 替换字符
        cleaned = cleaned.replaceAll("\\<\\|.*?\\|\\>", ""); // 移除类似 <|...|> 的标记
        cleaned = cleaned.replaceAll("\\<\\|image_.*?\\|\\>", ""); // 移除图像标记
        cleaned = cleaned.replaceAll("\\<\\|file_.*?\\|\\>", ""); // 移除文件标记

        // 移除重复的空白字符
        cleaned = cleaned.replaceAll("\\s+", " ");

        // 移除首尾空白
        cleaned = cleaned.trim();

        // 记录清理操作（仅在DEBUG模式下）
        if (log.isDebugEnabled() && !content.equals(cleaned)) {
            log.debug("AI响应内容已清理，原长度: {}, 清理后长度: {}", content.length(), cleaned.length());
        }

        return cleaned;
    }

    /**
     * 清理聊天响应中所有消息的内容
     * 应用到所有AI模型的响应结果
     *
     * @param response 原始聊天响应
     * @return 清理后的聊天响应
     */
    private ChatResponse cleanChatResponse(ChatResponse response) {
        if (response == null || response.getMessages() == null || response.getMessages().isEmpty()) {
            return response;
        }

        // 清理每条消息的内容
        for (ChatResponse.Message message : response.getMessages()) {
            if (message.getContent() != null) {
                String cleanedContent = cleanResponseContent(message.getContent());
                message.setContent(cleanedContent);
            }

        }

        return response;
    }

    /**
     * 生成请求ID
     *
     * @return 请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}