package com.contract.ai.feign.dto.glm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * GLM平台聊天响应DTO
 * 适配实际 GLM API 响应结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlmVisionChatResponse {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 请求创建时间，Unix 时间戳（秒）
     */
    private Integer created;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 模型响应列表
     */
    private List<Choice> choices;

    /**
     * Token使用统计
     */
    private Usage usage;

    /**
     * 视频结果
     */
    @JsonProperty("video_result")
    private List<VideoResult> videoResult;

    /**
     * 网络搜索结果
     */
    @JsonProperty("web_search")
    private List<WebSearch> webSearch;

    /**
     * 内容过滤结果
     */
    @JsonProperty("content_filter")
    private List<ContentFilter> contentFilter;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        /**
         * 结果索引
         */
        private Integer index;

        /**
         * 消息内容
         */
        private Message message;

        /**
         * 推理终止原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
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
         * 思维链内容
         */
        @JsonProperty("reasoning_content")
        private String reasoningContent;

        /**
         * 音频内容
         */
        private Audio audio;

        /**
         * 工具调用
         */
        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 用户输入的Token数量
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 输出的Token数量
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * Token总数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;

        /**
         * 缓存的Token数量
         */
        @JsonProperty("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptTokensDetails {
        /**
         * 命中的缓存Token数量
         */
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;
    }

    /**
     * 音频内容
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Audio {
        /**
         * 音频ID
         */
        private String id;

        /**
         * 音频数据
         */
        private String data;

        /**
         * 过期时间
         */
        @JsonProperty("expires_at")
        private String expiresAt;
    }

    /**
     * 工具调用
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
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

        /**
         * MCP信息
         */
        private Mcp mcp;
    }

    /**
     * 函数信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Function {
        /**
         * 函数名称
         */
        private String name;

        /**
         * 函数参数
         */
        private Map<String, Object> arguments;
    }

    /**
     * MCP信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mcp {
        /**
         * MCP ID
         */
        private String id;

        /**
         * MCP类型
         */
        private String type;

        /**
         * 服务器标签
         */
        @JsonProperty("server_label")
        private String serverLabel;

        /**
         * 错误信息
         */
        private String error;

        /**
         * 工具列表
         */
        private List<Tool> tools;

        /**
         * 参数
         */
        private String arguments;

        /**
         * 名称
         */
        private String name;

        /**
         * 输出
         */
        private Map<String, Object> output;
    }

    /**
     * MCP工具
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tool {
        /**
         * 工具名称
         */
        private String name;

        /**
         * 工具描述
         */
        private String description;

        /**
         * 注解
         */
        private Map<String, Object> annotations;

        /**
         * 输入模式
         */
        @JsonProperty("input_schema")
        private InputSchema inputSchema;
    }

    /**
     * 输入模式
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InputSchema {
        /**
         * 模式类型
         */
        private String type;

        /**
         * 属性
         */
        private Map<String, Object> properties;

        /**
         * 必需字段
         */
        private List<String> required;

        /**
         * 允许额外属性
         */
        @JsonProperty("additionalProperties")
        private Boolean additionalProperties;
    }

    /**
     * 视频结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoResult {
        /**
         * 视频URL
         */
        private String url;

        /**
         * 封面图片URL
         */
        @JsonProperty("cover_image_url")
        private String coverImageUrl;
    }

    /**
     * 网络搜索结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebSearch {
        /**
         * 图标
         */
        private String icon;

        /**
         * 标题
         */
        private String title;

        /**
         * 链接
         */
        private String link;

        /**
         * 媒体
         */
        private String media;

        /**
         * 发布日期
         */
        @JsonProperty("publish_date")
        private String publishDate;

        /**
         * 内容
         */
        private String content;

        /**
         * 来源
         */
        private String refer;
    }

    /**
     * 内容过滤结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentFilter {
        /**
         * 角色
         */
        private String role;

        /**
         * 过滤级别
         */
        private Integer level;
    }
}