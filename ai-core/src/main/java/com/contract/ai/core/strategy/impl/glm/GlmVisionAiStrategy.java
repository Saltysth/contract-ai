package com.contract.ai.core.strategy.impl.glm;

import com.contract.ai.core.exception.AiErrorCode;
import com.contract.ai.core.exception.AiException;
import com.contract.ai.core.registry.AiStrategyRegistry;
import com.contract.ai.core.service.FileStorageService;
import com.contract.ai.core.strategy.VisionAiStrategy;
import com.contract.ai.feign.convertor.GlmConvertor;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import com.contract.ai.feign.dto.glm.GlmVisionChatRequest;
import com.contract.ai.feign.dto.glm.GlmVisionChatResponse;
import com.contract.ai.feign.util.ImageCompressorWithThumbnailator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GLM视觉模型策略实现
 * 支持GLM-4.5V等视觉模型的多模态对话
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.strategy.glm.enabled", havingValue = "true")
@RequiredArgsConstructor
public class GlmVisionAiStrategy implements VisionAiStrategy {

    private final GlmVisionClient glmVisionClient;
    private final FileStorageService fileStorageService;
    private final GlmConvertor glmConvertor;
    private final AiStrategyRegistry strategyRegistry;

    /**
     * 支持的GLM视觉模型列表
     */
    @Value("${ai.strategy.glm.enabled-models:glm-4.1v-thinking-flash}")
    private List<String> enabledModels;

    /**
     * 模型图片数量限制映射
     */
    public static final Map<String, Integer> MODEL_IMAGE_LIMITS = new HashMap<>();

    static {
        MODEL_IMAGE_LIMITS.put("glm-4.1v-thinking-flash", 1);
        MODEL_IMAGE_LIMITS.put("glm-4v-plus-0111", 5);
    }

    /**
     * 图片压缩限制常量
     */
    private static final int MAX_WIDTH = 6000;  // 最大宽度 6k
    private static final int MAX_HEIGHT = 6000; // 最大高度 6k
    private static final int TARGET_SIZE_KB = 5 * 1024; // 5MB 限制 (5*1024 KB)


    @Value("${ai.strategy.glm.api-key:}")
    private String apiKey;



    @PostConstruct
    public GlmVisionAiStrategy glmVisionAiStrategy() {
        // 自动注册到策略注册中心
        strategyRegistry.register(this);

        log.info("Configured and registered GlmVisionAiStrategy strategy");
        return this;
    }


    @Override
    public List<String> getModel() {
        return enabledModels;
    }

    @Override
    public boolean supports(String model) {
        for (String supportedModel : enabledModels) {
            if (supportedModel.equals(model)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ChatResponse handleChat(ChatRequest request) {
        throw new UnsupportedOperationException("GLM视觉模型需要使用handleChatWithVision方法以支持文件参数");
    }

    @Override
    public ChatResponse handleChatWithVision(ChatRequest request, MultipartFile[] files) {
        try {
            log.info("处理GLM视觉模型请求，模型: {}", request.getModel());

            // 验证图片数量限制
            validateImageLimit(request.getModel(), files != null ? files.length : 0);

            // 压缩并存储上传的文件
            Map<String, String> fileContents = compressAndStoreFiles(files);
            log.info("成功压缩并存储{}个文件", fileContents.size());

            // 转换请求格式
            GlmVisionChatRequest glmRequest = glmConvertor.convertToGlmVisionRequest(request, fileContents);
            log.info("转换请求格式完成");

            // 调用GLM API
            GlmVisionChatResponse glmResponse = glmVisionClient.chatCompletions("Bearer " + getApiKey(), glmRequest);
            log.info("GLM API调用成功，响应ID: {}", glmResponse.getId());

            // 转换响应格式
            ChatResponse response = convertToChatResponse(glmResponse);
            log.info("GLM视觉模型 [{}] 返回回答内容: [{}]", request.getModel(),
                extractResponseContent(response));
            return response;

        } catch (Exception e) {
            log.error("GLM视觉模型处理失败", e);
            throw new AiException(AiErrorCode.AI_SERVICE_ERROR, e);
        }
    }

    /**
     * 使用base64编码处理GLM视觉模型请求
     * 直接将上传文件转换为base64格式，无需通过文件存储服务
     * 包含图片压缩功能，确保图片不超过6k*6k分辨率和5MB大小限制
     *
     * @param request 聊天请求
     * @param files 上传的文件数组
     * @return 聊天响应
     */
    public ChatResponse handleChatWithVisionBase64(ChatRequest request, MultipartFile[] files) {
        try {
            log.info("处理GLM视觉模型请求（base64编码），模型: {}", request.getModel());

            if (files == null || files.length == 0) {
                throw new IllegalArgumentException("上传文件不能为空");
            }

            // 验证图片数量限制
            validateImageLimit(request.getModel(), files.length);

            // 压缩文件并转换为base64格式
            Map<String, String> base64ImageMap = compressAndConvertFilesToBase64(files);
            log.info("成功压缩并转换{}个文件为base64格式", base64ImageMap.size());

            if (base64ImageMap.isEmpty()) {
                throw new AiException(AiErrorCode.AI_MISSING_REQUIRED_PARAMETER, "没有成功转换的图片文件，请检查文件格式和大小");
            }

            // 转换请求格式
            GlmVisionChatRequest glmRequest = glmConvertor.convertToGlmVisionRequest(request, base64ImageMap);
            log.info("转换请求格式完成");

            // 调用GLM API
            GlmVisionChatResponse glmResponse = glmVisionClient.chatCompletions("Bearer " + getApiKey(), glmRequest);
            log.info("GLM API调用成功，响应ID: {}", glmResponse.getId());

            // 转换响应格式
            ChatResponse response = convertToChatResponse(glmResponse);
            log.info("GLM视觉模型 [{}] 返回回答内容: [{}]", request.getModel(),
                extractResponseContent(response));
            return response;

        } catch (IllegalArgumentException e) {
            log.error("GLM视觉模型参数验证失败（base64编码）", e);
            throw new AiException(AiErrorCode.AI_MISSING_REQUIRED_PARAMETER, e.getMessage());
        } catch (Exception e) {
            log.error("GLM视觉模型处理失败（base64编码）", e);
            throw new AiException(AiErrorCode.AI_SERVICE_ERROR, e);
        }
    }

    /**
     * 处理带文件URL的聊天请求
     * 文件URL直接传递，无需压缩或base64转换
     *
     * @param request 聊天请求
     * @param fileMap 文件名到URL的映射
     * @return 聊天响应
     */
    public ChatResponse handleChatWithFiles(ChatRequest request, Map<String, String> fileMap) {
        try {
            log.info("处理GLM文件URL请求，模型: {}", request.getModel());

            if (fileMap == null || fileMap.isEmpty()) {
                throw new IllegalArgumentException("文件URL数据不能为空");
            }

            log.info("成功接收到{}个文件URL", fileMap.size());

            // 转换请求格式
            GlmVisionChatRequest glmRequest = glmConvertor.convertToGlmVisionRequest(request, fileMap);
            log.info("转换请求格式完成");

            // 调用GLM API
            GlmVisionChatResponse glmResponse = glmVisionClient.chatCompletions("Bearer " + getApiKey(), glmRequest);
            log.info("GLM API调用成功，响应ID: {}", glmResponse.getId());

            // 转换响应格式
            ChatResponse response = convertToChatResponse(glmResponse);
            log.info("GLM文件处理模型 [{}] 返回回答内容: [{}]", request.getModel(),
                extractResponseContent(response));
            return response;

        } catch (Exception e) {
            log.error("GLM文件处理失败", e);
            throw new AiException(AiErrorCode.AI_SERVICE_ERROR, e);
        }
    }

    @Override
    public ChatResponse handleChatWithVision(ChatRequest request, Map<String, String> imageMap) {
        try {
            log.info("处理GLM视觉模型请求（base64图片），模型: {}", request.getModel());

            if (imageMap == null || imageMap.isEmpty()) {
                throw new IllegalArgumentException("base64图片数据不能为空");
            }

            // 验证图片数量限制
            validateImageLimit(request.getModel(), imageMap.size());

            log.info("成功接收到{}张base64图片", imageMap.size());

            // 转换请求格式
            GlmVisionChatRequest glmRequest = glmConvertor.convertToGlmVisionRequest(request, imageMap);
            log.info("转换请求格式完成");

            // 调用GLM API
            GlmVisionChatResponse glmResponse = glmVisionClient.chatCompletions("Bearer " + getApiKey(), glmRequest);
            log.info("GLM API调用成功，响应ID: {}", glmResponse.getId());

            // 转换响应格式
            ChatResponse response = convertToChatResponse(glmResponse);
            log.info("GLM视觉模型 [{}] 返回回答内容: [{}]", request.getModel(),
                extractResponseContent(response));
            return response;

        } catch (Exception e) {
            log.error("GLM视觉模型处理失败（base64图片）", e);
            throw new AiException(AiErrorCode.AI_SERVICE_ERROR, e);
        }
    }

    /**
     * 验证图片数量限制
     *
     * @param model 模型名称
     * @param imageCount 图片数量
     */
    private void validateImageLimit(String model, int imageCount) {
        Integer limit = MODEL_IMAGE_LIMITS.get(model);
        if (limit == null) {
            log.warn("未找到模型 {} 的图片数量限制配置，使用默认限制1张", model);
            limit = 1; // 默认限制
        }

        if (imageCount > limit) {
            String errorMessage = String.format("模型 %s 仅支持最多 %d 张图片，当前上传了 %d 张图片", model, limit, imageCount);
            log.error(errorMessage);
            throw new AiException(AiErrorCode.AI_MISSING_REQUIRED_PARAMETER, errorMessage);
        }

        log.info("模型 {} 图片数量验证通过，限制: {} 张，实际: {} 张", model, limit, imageCount);
    }

    /**
     * 获取GLM API密钥
     */
    private String getApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AiException(AiErrorCode.AI_AUTHENTICATION_FAILED, "GLM API密钥未配置，请设置GLM_API_KEY环境变量");
        }
        return apiKey;
    }

    /**
     * 将GLM响应转换为通用ChatResponse格式
     */
    private ChatResponse convertToChatResponse(GlmVisionChatResponse glmResponse) {
        if (glmResponse.getChoices() == null || glmResponse.getChoices().isEmpty()) {
            throw new AiException(AiErrorCode.AI_RESPONSE_FORMAT_ERROR, "GLM API返回空响应");
        }

        GlmVisionChatResponse.Choice firstChoice = glmResponse.getChoices().get(0);
        String content = firstChoice.getMessage() != null ? firstChoice.getMessage().getContent() : "";

        // 注意：响应内容清理现在在ChatService层的cleanChatResponse方法中统一处理

        // 构建响应消息
        ChatResponse.Message responseMessage = new ChatResponse.Message("assistant", content);

        // 设置思考内容（如果有）
        if (firstChoice.getMessage() != null && firstChoice.getMessage().getReasoningContent() != null) {
            // 注意：响应内容清理现在在ChatService层的cleanChatResponse方法中统一处理
            responseMessage.setExtensions(Map.of("reasoning_content", firstChoice.getMessage().getReasoningContent()));
        }

        // 构建完整响应
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setId(glmResponse.getId());
        chatResponse.setModel(glmResponse.getModel());
        chatResponse.setMessages(List.of(responseMessage));

        // 设置使用情况
        if (glmResponse.getUsage() != null) {
            ChatResponse.Usage usage = new ChatResponse.Usage();
            usage.setPromptTokens(glmResponse.getUsage().getPromptTokens());
            usage.setCompletionTokens(glmResponse.getUsage().getCompletionTokens());
            usage.setTotalTokens(glmResponse.getUsage().getTotalTokens());
            chatResponse.setUsage(usage);
        }

        return chatResponse;
    }

    /**
     * 压缩文件并转换为base64格式
     * @param files 上传的文件数组
     * @return 文件名到base64数据的映射
     */
    private Map<String, String> compressAndConvertFilesToBase64(MultipartFile[] files) {
        Map<String, String> base64ImageMap = new HashMap<>();

        if (files == null || files.length == 0) {
            return base64ImageMap;
        }

        int fileIndex = 1;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    // 压缩文件
                    byte[] compressedBytes = ImageCompressorWithThumbnailator.compressMultipartFile(
                            file, MAX_WIDTH, MAX_HEIGHT, TARGET_SIZE_KB);

                    // 转换为base64
                    String mimeType = file.getContentType();
                    if (mimeType == null || !mimeType.startsWith("image/")) {
                        log.warn("跳过非图片文件: {}", file.getOriginalFilename());
                        continue;
                    }

                    // 构建完整的base64 URL格式
                    String base64Raw = java.util.Base64.getEncoder().encodeToString(compressedBytes);
                    String base64Image = String.format("data:%s;base64,%s", mimeType, base64Raw);

                    // 验证并标准化格式
                    com.contract.ai.feign.util.ImageBase64Validator.ValidationResult validation =
                        com.contract.ai.feign.util.ImageBase64Validator.validateBase64Image(base64Image);
                    if (!validation.isValid()) {
                        log.warn("生成的base64格式验证失败: {}", validation.getErrorMessage());
                        continue;
                    }

                    String base64Data = com.contract.ai.feign.util.ImageBase64Validator.normalizeBase64Image(base64Image);

                    String fileName = "image_" + fileIndex + getImageExtension(file.getOriginalFilename());
                    base64ImageMap.put(fileName, base64Data);
                    fileIndex++;

                    log.info("文件压缩转换完成: {}, 原始大小: {} KB, 压缩后大小: {} KB",
                            file.getOriginalFilename(),
                            ImageCompressorWithThumbnailator.getFileSizeKB(file),
                            compressedBytes.length / 1024);

                } catch (IOException e) {
                    log.error("压缩文件失败: {}", file.getOriginalFilename(), e);
                    // 继续处理其他文件，不因单个文件失败而中断
                }
            }
        }

        return base64ImageMap;
    }

    /**
     * 压缩并存储文件
     * 直接将压缩后的字节数组传递给存储服务
     * @param files 上传的文件数组
     * @return 文件名到文件内容的映射
     */
    private Map<String, String> compressAndStoreFiles(MultipartFile[] files) {
        Map<String, String> fileContents = new HashMap<>();

        if (files == null || files.length == 0) {
            return fileContents;
        }

        // 优先尝试压缩文件
        Map<String, byte[]> compressedFiles = new HashMap<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    // 压缩文件
                    byte[] compressedBytes = ImageCompressorWithThumbnailator.compressMultipartFile(
                            file, MAX_WIDTH, MAX_HEIGHT, TARGET_SIZE_KB);

                    String fileName = "compressed_" + file.getOriginalFilename();
                    compressedFiles.put(fileName, compressedBytes);

                    log.info("文件压缩完成: {}, 原始大小: {} KB, 压缩后大小: {} KB",
                            file.getOriginalFilename(),
                            ImageCompressorWithThumbnailator.getFileSizeKB(file),
                            compressedBytes.length / 1024);

                } catch (IOException e) {
                    log.error("压缩文件失败: {}", file.getOriginalFilename(), e);
                    // 压缩失败时，保留原文件用于后续处理
                    try {
                        compressedFiles.put(file.getOriginalFilename(), file.getBytes());
                        log.warn("压缩失败，使用原文件: {}", file.getOriginalFilename());
                    } catch (IOException ex) {
                        log.error("读取原文件失败: {}", file.getOriginalFilename(), ex);
                    }
                }
            }
        }

        // 将压缩后的文件直接转换为base64，跳过存储步骤
        int fileIndex = 1;
        for (Map.Entry<String, byte[]> entry : compressedFiles.entrySet()) {
            String fileName = entry.getKey();
            byte[] fileBytes = entry.getValue();

            // 转换为base64
            String mimeType = "image/jpeg"; // 默认mimetype
            if (fileName.toLowerCase().endsWith(".png")) {
                mimeType = "image/png";
            } else if (fileName.toLowerCase().endsWith(".gif")) {
                mimeType = "image/gif";
            } else if (fileName.toLowerCase().endsWith(".webp")) {
                mimeType = "image/webp";
            }

            // 构建完整的base64 URL格式
            String base64Raw = java.util.Base64.getEncoder().encodeToString(fileBytes);
            String base64Image = String.format("data:%s;base64,%s", mimeType, base64Raw);

            // 验证并标准化格式
            com.contract.ai.feign.util.ImageBase64Validator.ValidationResult validation =
                com.contract.ai.feign.util.ImageBase64Validator.validateBase64Image(base64Image);
            if (!validation.isValid()) {
                log.warn("生成的base64格式验证失败: {}", validation.getErrorMessage());
                continue;
            }

            String base64Data = com.contract.ai.feign.util.ImageBase64Validator.normalizeBase64Image(base64Image);

            String outputFileName = "image_" + fileIndex + getImageExtension(fileName);
            fileContents.put(outputFileName, base64Data);
            fileIndex++;
        }

        return fileContents;
    }

    /**
     * 根据原始文件名获取图片扩展名
     */
    private String getImageExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ".jpg";
        }

        String lowerCaseName = originalFilename.toLowerCase();
        if (lowerCaseName.endsWith(".png")) {
            return ".png";
        } else if (lowerCaseName.endsWith(".gif")) {
            return ".gif";
        } else if (lowerCaseName.endsWith(".webp")) {
            return ".webp";
        } else {
            return ".jpg"; // 默认使用jpg
        }
    }

    @Override
    public int getPriority() {
        return 10; // 设置较高优先级
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