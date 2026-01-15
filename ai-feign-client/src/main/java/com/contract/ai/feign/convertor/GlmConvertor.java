package com.contract.ai.feign.convertor;

import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.glm.GlmVisionChatRequest;
import com.contract.ai.feign.dto.glm.MultimodalityContentItem;
import com.contract.ai.feign.util.ImageBase64Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GLM平台转换器
 * 负责将通用ChatRequest转换为GLM专用格式
 */
@Slf4j
@Component("glmConvertor")
public class GlmConvertor {

    /**
     * 将通用ChatRequest转换为GLM视觉模型请求
     *
     * @param chatRequest 通用聊天请求
     * @param fileContents 文件内容映射（文件名 -> 文件URL或base64内容）
     * @return GLM视觉模型请求
     */
    public GlmVisionChatRequest convertToGlmVisionRequest(ChatRequest chatRequest, Map<String, String> fileContents) {
        List<GlmVisionChatRequest.VisionMessage> visionMessages = new ArrayList<>();

        for (ChatRequest.Message message : chatRequest.getMessages()) {
            if (hasFileAttachments(message)) {
                // 处理包含文件附件的多模态消息
                GlmVisionChatRequest.VisionMessage visionMessage = convertToMultimodalityMessage(message, fileContents);
                visionMessages.add(visionMessage);
            } else {
                // 处理纯文本消息
                String textContent = message.getTextContent();
                if (textContent != null && !textContent.trim().isEmpty()) {
                    GlmVisionChatRequest.VisionMessage visionMessage = GlmVisionChatRequest.VisionMessage
                            .textMessage(message.getRole(), textContent);
                    visionMessages.add(visionMessage);
                }
            }
        }

        // 配置思考模式
        GlmVisionChatRequest.ChatThinking thinking = null;
        if (chatRequest.getModel() != null && chatRequest.getModel().contains("thinking")) {
            thinking = GlmVisionChatRequest.ChatThinking.builder()
                    .type("enabled")
                    .build();
        }

        return GlmVisionChatRequest.builder()
                .model(chatRequest.getModel())
                .messages(visionMessages)
                .stream(chatRequest.getStream())
                .thinking(thinking)
                .doSample(true)
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
                .maxTokens(chatRequest.getMaxTokens())
                .stop(chatRequest.getStop())
                .requestId(chatRequest.getExtensions() != null ?
                    (String) chatRequest.getExtensions().get("request_id") : null)
                .userId(chatRequest.getExtensions() != null ?
                    (String) chatRequest.getExtensions().get("user_id") : null)
                .build();
    }

    /**
     * 将通用消息转换为多模态消息
     * 支持新的content数组和旧的文件扩展结构
     */
    private GlmVisionChatRequest.VisionMessage convertToMultimodalityMessage(
            ChatRequest.Message message,
            Map<String, String> fileContents) {

        List<MultimodalityContentItem> contentItems = new ArrayList<>();

        // 优先处理新的多模态content数组结构
        if (message.isMultimodal()) {
            List<ChatRequest.Message.ContentItem> originalItems = message.getMultimodalContent();

            for (ChatRequest.Message.ContentItem originalItem : originalItems) {
                if ("text".equals(originalItem.getType())) {
                    // 文本内容项
                    MultimodalityContentItem textContent = MultimodalityContentItem.builder()
                            .type("text")
                            .text(originalItem.getText())
                            .build();
                    contentItems.add(textContent);

                } else if ("image_url".equals(originalItem.getType())) {
                    // 图片内容项
                    String imageUrl = originalItem.getImage_url().getUrl();

                    try {
                        // 标准化base64图片格式
                        String normalizedImage = ImageBase64Validator.normalizeBase64Image(imageUrl);
                        if (normalizedImage == null) {
                            log.warn("无法标准化base64图片格式，跳过该图片");
                            continue;
                        }

                        // 验证base64图片格式
                        ImageBase64Validator.ValidationResult validation =
                            ImageBase64Validator.validateBase64Image(normalizedImage);

                        if (!validation.isValid()) {
                            log.warn("base64图片格式验证失败: {}，跳过该图片", validation.getErrorMessage());
                            continue;
                        }

                        MultimodalityContentItem imageContent = MultimodalityContentItem.builder()
                                .type("image_url")
                                .imageUrl(MultimodalityContentItem.ImageUrl.builder()
                                        .url(normalizedImage)
                                        .build())
                                .build();
                        contentItems.add(imageContent);
                        log.debug("成功添加图片内容项，格式大小: {} 字符", normalizedImage.length());

                    } catch (Exception e) {
                        log.warn("处理图片时发生错误，跳过该图片: {}", e.getMessage());
                        // 继续处理其他内容，不中断整个请求
                    }
                } else if ("file_url".equals(originalItem.getType())) {
                    // 文件内容项
                    String fileUrl = originalItem.getFile_url().getUrl();

                    try {
                        MultimodalityContentItem fileContent = MultimodalityContentItem.builder()
                                .type("file_url")
                                .fileUrl(MultimodalityContentItem.FileUrl.builder()
                                        .url(fileUrl)
                                        .build())
                                .build();
                        contentItems.add(fileContent);
                        log.debug("成功添加文件内容项: {}", fileUrl);

                    } catch (Exception e) {
                        log.warn("处理文件时发生错误，跳过该文件: {}", e.getMessage());
                        // 继续处理其他内容，不中断整个请求
                    }
                }
            }
        } else {
            // 处理纯文本内容（向后兼容）
            String textContent = message.getTextContent();
            if (textContent != null && !textContent.trim().isEmpty()) {
                MultimodalityContentItem textItem = MultimodalityContentItem.builder()
                        .type("text")
                        .text(textContent)
                        .build();
                contentItems.add(textItem);
            }
        }

        // 处理旧的文件扩展结构（向后兼容）
        if (message.getExtensions() != null && message.getExtensions().containsKey("files")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> files = (List<Map<String, Object>>) message.getExtensions().get("files");

            for (Map<String, Object> file : files) {
                String fileName = (String) file.get("name");
                String fileType = (String) file.get("type");
                String fileUrl = fileContents.get(fileName);

                if (fileUrl != null) {
                    MultimodalityContentItem fileContent = createFileContentItem(fileType, fileUrl);
                    if (fileContent != null) {
                        contentItems.add(fileContent);
                    }
                }
            }
        }

        // 如果没有内容项，创建一个空的文本项
        if (contentItems.isEmpty()) {
            contentItems.add(MultimodalityContentItem.builder()
                    .type("text")
                    .text("")
                    .build());
        }

        return GlmVisionChatRequest.VisionMessage
                .multimodalityMessage(message.getRole(), contentItems);
    }

    /**
     * 根据文件类型创建对应的内容项
     * 注意：本服务只支持base64格式的图片
     */
    private MultimodalityContentItem createFileContentItem(String fileType, String fileUrl) {
        if (fileUrl == null) {
            log.warn("文件URL为空，跳过该文件");
            return null;
        }

        // 根据文件扩展名或MIME类型判断文件类型
        String lowerFileType = fileType.toLowerCase();

        if (isImageFile(lowerFileType)) {
            try {
                // 标准化base64图片格式
                String normalizedImage = ImageBase64Validator.normalizeBase64Image(fileUrl);
                if (normalizedImage == null) {
                    log.warn("无法标准化图片base64格式，跳过该文件: {}", fileType);
                    return null;
                }

                // 验证base64图片格式
                ImageBase64Validator.ValidationResult validation = ImageBase64Validator.validateBase64Image(normalizedImage);
                if (!validation.isValid()) {
                    log.warn("图片格式验证失败: {}，跳过该文件: {}", validation.getErrorMessage(), fileType);
                    return null;
                }

                return MultimodalityContentItem.builder()
                        .type("image_url")
                        .imageUrl(MultimodalityContentItem.ImageUrl.builder()
                                .url(normalizedImage)
                                .build())
                        .build();
            } catch (Exception e) {
                log.warn("处理图片文件时发生错误，跳过该文件: {}, 错误: {}", fileType, e.getMessage());
                return null;
            }
        } else if (isVideoFile(lowerFileType)) {
            return MultimodalityContentItem.builder()
                    .type("video_url")
                    .videoUrl(MultimodalityContentItem.VideoUrl.builder()
                            .url(fileUrl)
                            .build())
                    .build();
        } else if (isDocumentFile(lowerFileType)) {
            return MultimodalityContentItem.builder()
                    .type("file_url")
                    .fileUrl(MultimodalityContentItem.FileUrl.builder()
                            .url(fileUrl)
                            .build())
                    .build();
        } else {
            log.warn("不支持的文件类型: {}，跳过该文件", fileType);
            return null;
        }
    }

    /**
     * 检查消息是否包含文件附件或图片
     */
    private boolean hasFileAttachments(ChatRequest.Message message) {
        // 检查新的多模态结构
        if (message.isMultimodal()) {
            return message.getMultimodalContent().stream()
                .anyMatch(item -> "image_url".equals(item.getType()) || "file_url".equals(item.getType()));
        }

        // 检查旧的文件扩展结构（向后兼容）
        return message.getExtensions() != null &&
               message.getExtensions().containsKey("files") &&
               message.getExtensions().get("files") instanceof List;
    }

    /**
     * 判断是否为图片文件
     * 支持更多图片格式
     */
    private boolean isImageFile(String fileType) {
        return fileType.endsWith(".jpg") ||
               fileType.endsWith(".jpeg") ||
               fileType.endsWith(".png") ||
               fileType.endsWith(".webp") ||
               fileType.endsWith(".gif") ||
               fileType.endsWith(".bmp") ||
               fileType.endsWith(".tiff") ||
               fileType.endsWith(".tif") ||
               fileType.startsWith("image/");
    }

    /**
     * 判断是否为视频文件
     */
    private boolean isVideoFile(String fileType) {
        return fileType.endsWith(".mp4") || fileType.startsWith("video/");
    }

    /**
     * 判断是否为文档文件
     */
    private boolean isDocumentFile(String fileType) {
        return fileType.endsWith(".pdf") ||
               fileType.endsWith(".doc") ||
               fileType.endsWith(".docx") ||
               fileType.startsWith("application/");
    }

    /**
     * 从请求中提取文件信息
     */
    public Map<String, Object> extractFileInfo(ChatRequest chatRequest) {
        Map<String, Object> fileInfo = new HashMap<>();

        for (ChatRequest.Message message : chatRequest.getMessages()) {
            if (hasFileAttachments(message)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> files = (List<Map<String, Object>>) message.getExtensions().get("files");
                fileInfo.put("files", files);
                break; // 假设只有一条消息包含文件
            }
        }

        return fileInfo;
    }

    /**
     * 生成标准的base64图片格式
     *
     * @param base64Data base64图片数据
     * @param imageFormat 图片格式 (jpeg, png, jpg, webp, gif, bmp, tiff等)
     * @return 标准格式的base64图片字符串
     */
    public static String generateBase64ImageUrl(String base64Data, String imageFormat) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new IllegalArgumentException("base64数据不能为空");
        }

        // 如果已经是标准格式，先验证和标准化
        if (base64Data.startsWith("data:image/")) {
            String normalizedImage = ImageBase64Validator.normalizeBase64Image(base64Data);
            if (normalizedImage != null) {
                // 验证格式是否正确
                ImageBase64Validator.ValidationResult validation = ImageBase64Validator.validateBase64Image(normalizedImage);
                if (validation.isValid()) {
                    return normalizedImage;
                }
            }
            log.warn("base64图片格式验证失败，尝试重新生成格式");
        }

        // 转换为标准格式
        String normalizedFormat = normalizeImageFormat(imageFormat);
        String standardFormat = String.format("data:image/%s;base64,%s", normalizedFormat, base64Data.trim());

        // 验证生成的格式
        ImageBase64Validator.ValidationResult validation = ImageBase64Validator.validateBase64Image(standardFormat);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("生成的base64图片格式不正确: " + validation.getErrorMessage());
        }

        return standardFormat;
    }

    /**
     * 标准化图片格式名称
     */
    private static String normalizeImageFormat(String format) {
        if (format == null) {
            return "jpeg"; // 默认格式
        }

        String lowerFormat = format.toLowerCase().replace(".", "");
        switch (lowerFormat) {
            case "jpg":
            case "jpeg":
                return "jpeg";
            case "png":
                return "png";
            case "webp":
                return "webp";
            case "gif":
                return "gif";
            case "bmp":
                return "bmp";
            case "tiff":
            case "tif":
                return "tiff";
            default:
                log.debug("未知图片格式: {}，使用默认jpeg格式", format);
                return "jpeg"; // 默认格式
        }
    }
}