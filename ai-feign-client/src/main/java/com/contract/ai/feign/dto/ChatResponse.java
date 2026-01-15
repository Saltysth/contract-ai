package com.contract.ai.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天响应DTO
 */
@Data
public class ChatResponse {

    /**
     * 响应ID
     */
    private String id;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 创建时间戳
     */
    @JsonProperty("created")
    private LocalDateTime created;

    /**
     * 响应状态
     */
    private String status;

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 使用的token数统计
     */
    private Usage usage;

    /**
     * 扩展参数
     * 用于存储平台特定的响应数据，支持灵活扩展
     */
    private Map<String, Object> extensions;

    /**
     * 消息内容
     */
    @Data
    public static class Message {

        /**
         * 消息角色
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 消息扩展参数
         * 用于支持特定平台的消息格式扩展
         */
        private Map<String, Object> extensions;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public Message(String role, String content, Map<String, Object> extensions) {
            this.role = role;
            this.content = content;
            this.extensions = extensions;
        }
    }

    /**
     * Token使用统计
     */
    @Data
    public static class Usage {

        /**
         * 提示token数
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成token数
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总token数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;

        /**
         * 使用统计扩展参数
         * 用于支持特定平台的额外使用统计信息
         */
        private Map<String, Object> extensions;
    }
}