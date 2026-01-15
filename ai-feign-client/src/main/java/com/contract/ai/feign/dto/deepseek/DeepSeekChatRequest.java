package com.contract.ai.feign.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 平台专用聊天请求
 */
@Data
@Builder
public class DeepSeekChatRequest {

    /**
     * 模型名称 (deepseek-chat, deepseek-reasoner)
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
     * 响应格式 - DeepSeek 特有参数
     */
    private ResponseFormat responseFormat;

    /**
     * 结果数量
     */
    @Builder.Default
    private Integer n = 1;

    /**
     * 频率惩罚
     */
    @Builder.Default
    private Double frequencyPenalty = 0.0;

    /**
     * 存在惩罚
     */
    @Builder.Default
    private Double presencePenalty = 0.0;

    /**
     * 消息内容
     */
    @Data
    @Builder
    public static class Message {

        /**
         * 消息角色 (system, user, assistant)
         */
        @NotBlank(message = "消息角色不能为空")
        private String role;

        /**
         * 消息内容
         */
        @NotBlank(message = "消息内容不能为空")
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * 响应格式 - 支持 json_object 输出
     */
    @Data
    public static class ResponseFormat {
        /**
         * 响应类型，支持 "text" 或 "json_object"
         */
        private String type;

        public ResponseFormat() {}

        public ResponseFormat(String type) {
            this.type = type;
        }
    }
}