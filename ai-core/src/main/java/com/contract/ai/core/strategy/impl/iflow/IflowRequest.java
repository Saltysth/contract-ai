package com.contract.ai.core.strategy.impl.iflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 心流平台请求DTO
 * 适配心流平台API格式
 */
@Data
public class IflowRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 生成结果的最大token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 采样温度，控制随机性 (0.0-2.0)
     */
    private Double temperature;

    /**
     * 采样多样性，控制随机性 (0.0-1.0)
     */
    private Double topP;

    /**
     * 流式输出
     */
    private Boolean stream;

    /**
     * 停止词列表
     */
    private List<String> stop;

    /**
     * 消息内容
     */
    @Data
    public static class Message {

        /**
         * 消息角色 (system, user, assistant)
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}