package com.contract.ai.core.strategy.impl.iflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 心流平台响应DTO
 * 适配心流平台API格式
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IflowResponse {

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
    private Long created;

    /**
     * 对象类型
     */
    private String object;

    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * 工具调用列表
     */
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    /**
     * 使用的token数统计
     */
    private Usage usage;

    /**
     * 选择内容
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        /**
         * 选择索引
         */
        private Integer index;

        /**
         * 消息内容
         */
        private Message message;

        /**
         * 完成原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;
    }

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
         * 推理内容
         */
        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

    /**
     * 工具调用
     */
    @Data
    public static class ToolCall {

        /**
         * 工具调用ID
         */
        private String id;

        /**
         * 工具调用类型
         */
        private String type;

        /**
         * 函数信息
         */
        private Function function;
    }

    /**
     * 函数信息
     */
    @Data
    public static class Function {

        /**
         * 函数名称
         */
        private String name;

        /**
         * 函数参数
         */
        private String arguments;
    }

    /**
     * Token使用统计
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    }
}