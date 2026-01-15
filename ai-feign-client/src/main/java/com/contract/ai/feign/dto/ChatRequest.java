package com.contract.ai.feign.dto;

import com.contract.ai.feign.enums.PlatFormType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 聊天请求DTO
 * 固定签名chat(ChatRequest)对外契约
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    /**
     * 平台类型，用于选择具体策略
     */
    @NotNull(message = "平台类型不能为空")
    private PlatFormType platform;

    /**
     * 模型名称，作为路由键
     */
    @NotBlank(message = "模型名称不能为空")
    private String model;

    /**
     * 消息列表
     */
    @NotNull(message = "消息列表不能为空")
    private List<Message> messages;

    /**
     * 生成结果的最大token数
     */
    @JsonProperty("max_tokens")
    @Builder.Default
    private Integer maxTokens = 2048;

    /**
     * 采样温度，控制随机性 (0.0-2.0)
     */
    @Builder.Default
    private Double temperature = 1.0;

    /**
     * 采样多样性，控制随机性 (0.0-1.0)
     */
    @Builder.Default
    private Double topP = 0.7;

    /**
     * 流式输出
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 停止词列表
     */
    private List<String> stop;

    /**
     * 响应格式
     */
    private ResponseReformat responseReformat;

    /**
     * 结果数量
     */
    @Builder.Default
    private int n = 1;

    /**
     * 扩展参数
     * 用于存储平台特定的额外参数，支持灵活扩展
     */
    private Map<String, Object> extensions;

    /**
     * 消息内容
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        /**
         * 消息角色 (system, user, assistant)
         */
        @NotBlank(message = "消息角色不能为空")
        private String role;

        /**
         * 消息内容
         * 可以是：
         * 1. 纯文本字符串（向后兼容）
         * 2. 多模态内容数组（新格式）
         */
        private Object content;

        /**
         * 消息扩展参数
         * 用于支持特定平台的消息格式扩展
         * @deprecated 推荐直接使用 content 数组格式传递多模态内容
         */
        @Deprecated
        private Map<String, Object> extensions;

        /**
         * 创建纯文本消息（向后兼容）
         */
        public static Message textMessage(String role, String content) {
            return Message.builder()
                    .role(role)
                    .content(content)
                    .build();
        }

        /**
         * 创建多模态消息
         */
        public static Message multimodalMessage(String role, List<ContentItem> contentItems) {
            return Message.builder()
                    .role(role)
                    .content(contentItems)
                    .build();
        }

        /**
         * 检查消息是否为多模态内容
         */
        public boolean isMultimodal() {
            return content instanceof List;
        }

        /**
         * 获取多模态内容列表
         * 处理反序列化后的类型转换问题
         */
        @SuppressWarnings("unchecked")
        public List<ContentItem> getMultimodalContent() {
            if (!isMultimodal()) {
                return null;
            }

            List<Object> rawList = (List<Object>) content;
            List<ContentItem> contentItems = new ArrayList<>();

            for (Object item : rawList) {
                if (item instanceof ContentItem) {
                    contentItems.add((ContentItem) item);
                } else if (item instanceof Map) {
                    // 处理反序列化为Map的情况
                    try {
                        // 使用Jackson将Map转换为ContentItem
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        ContentItem contentItem = mapper.convertValue(item, ContentItem.class);
                        contentItems.add(contentItem);
                    } catch (Exception e) {
                        // 如果转换失败，创建一个默认的文本项
                        ContentItem fallbackItem = ContentItem.text(item.toString());
                        contentItems.add(fallbackItem);
                    }
                } else {
                    // 其他情况，创建默认的文本项
                    ContentItem fallbackItem = ContentItem.text(item.toString());
                    contentItems.add(fallbackItem);
                }
            }

            return contentItems;
        }

        /**
         * 获取文本内容（向后兼容）
         */
        public String getTextContent() {
            if (content instanceof String) {
                return (String) content;
            } else if (isMultimodal()) {
                // 从多模态内容中提取文本
                List<ContentItem> items = getMultimodalContent();
                if (items != null) {
                    StringBuilder textBuilder = new StringBuilder();
                    for (ContentItem item : items) {
                        if ("text".equals(item.getType()) && item.getText() != null) {
                            textBuilder.append(item.getText());
                        }
                    }
                    return textBuilder.toString();
                }
            }
            return null;
        }

        /**
         * 内容项
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ContentItem {
            /**
             * 内容类型：text, image_url, file_url
             */
            private String type;

            /**
             * 文本内容（当type为text时使用）
             */
            private String text;

            /**
             * 图片URL信息（当type为image_url时使用）
             */
            private ImageUrlInfo image_url;

            /**
             * 文件URL信息（当type为file_url时使用）
             */
            private FileUrlInfo file_url;

            /**
             * 创建文本内容项
             */
            public static ContentItem text(String text) {
                return ContentItem.builder()
                        .type("text")
                        .text(text)
                        .build();
            }

            /**
             * 创建图片内容项
             */
            public static ContentItem imageUrl(String url) {
                return ContentItem.builder()
                        .type("image_url")
                        .image_url(ImageUrlInfo.builder()
                                .url(url)
                                .build())
                        .build();
            }

            /**
             * 创建文件内容项
             */
            public static ContentItem fileUrl(String url) {
                return ContentItem.builder()
                        .type("file_url")
                        .file_url(FileUrlInfo.builder()
                                .url(url)
                                .build())
                        .build();
            }
        }

        /**
         * 图片URL信息
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ImageUrlInfo {
            /**
             * 图片URL或base64数据
             * 格式：data:image/[format];base64,[base64_data]
             */
            private String url;
        }

        /**
         * 文件URL信息
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FileUrlInfo {
            /**
             * 文件URL地址，不支持Base64编码
             * 支持PDF、Word等格式，最多支持50个
             */
            private String url;
        }
      }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseReformat {
        private String type;
    }
}