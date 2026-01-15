package com.contract.ai.feign.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek 平台专用聊天响应
 * 适配实际 DeepSeek API 响应结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepSeekChatResponse {

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
     * 使用的token数统计
     */
    private Usage usage;

    /**
     * 系统指纹
     */
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    /**
     * 选择内容
     */
    @Data
    public static class Choice {

        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息对象
         */
        private Message message;

        /**
         * 完成原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;

        /**
         * 日志概率
         */
        @JsonProperty("logprobs")
        private Object logprobs;
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

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
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

        /**
         * 提示token详情
         */
        @JsonProperty("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;

        /**
         * 缓存命中的token数
         */
        @JsonProperty("prompt_cache_hit_tokens")
        private Integer promptCacheHitTokens;

        /**
         * 缓存未命中的token数
         */
        @JsonProperty("prompt_cache_miss_tokens")
        private Integer promptCacheMissTokens;
    }

    /**
     * 提示token详情
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromptTokensDetails {

        /**
         * 缓存的token数
         */
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;
    }
}