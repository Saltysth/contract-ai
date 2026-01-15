package com.contract.ai.feign.dto.glm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * GLM视觉模型聊天请求DTO
 * 支持多模态内容（文本、图片、视频、文件）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlmVisionChatRequest {

    /**
     * 模型名称
     * 支持的视觉模型：glm-4.5v, glm-4v-plus-0111, glm-4v-flash, glm-4.1v-thinking-flashx, glm-4.1v-thinking-flash
     */
    private String model;

    /**
     * 对话消息列表
     */
    private List<VisionMessage> messages;

    /**
     * 是否启用流式输出模式
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 思考模式配置
     */
    private ChatThinking thinking;

    /**
     * 是否启用采样策略来生成文本
     */
    @JsonProperty("do_sample")
    @Builder.Default
    private Boolean doSample = true;

    /**
     * 采样温度，控制输出的随机性和创造性
     */
    @Builder.Default
    private Double temperature = 0.8;

    /**
     * 核采样参数
     */
    @JsonProperty("top_p")
    @Builder.Default
    private Double topP = 0.6;

    /**
     * 模型输出的最大令牌token数量限制
     */
    @JsonProperty("max_tokens")
    @Builder.Default
    private Integer maxTokens = 1024;

    /**
     * 停止词列表
     */
    private List<String> stop;

    /**
     * 请求唯一标识符
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 终端用户的唯一标识符
     */
    @JsonProperty("user_id")
    private String userId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisionMessage {
        /**
         * 消息角色：user, system, assistant
         */
        private String role;

        /**
         * 消息内容
         * 可以是字符串文本或多模态内容数组
         */
        private Object content;

        /**
         * 创建纯文本消息
         */
        public static VisionMessage textMessage(String role, String text) {
            return VisionMessage.builder()
                    .role(role)
                    .content(text)
                    .build();
        }

        /**
         * 创建多模态消息
         */
        public static VisionMessage multimodalityMessage(String role, List<MultimodalityContentItem> contents) {
            return VisionMessage.builder()
                    .role(role)
                    .content(contents)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatThinking {
        /**
         * 思考模式类型
         * enabled: 开启思考链
         * disabled: 关闭思考链
         */
        @Builder.Default
        private String type = "enabled";
    }
}