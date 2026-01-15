package com.contract.ai.feign.dto.glm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GLM多模态内容项
 * 支持文本、图片、视频、文件等多种内容类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultimodalityContentItem {

    /**
     * 内容类型：text, image_url, video_url, file_url
     */
    private String type;

    /**
     * 文本内容
     */
    private String text;

    /**
     * 图片URL信息
     */
    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    /**
     * 视频URL信息
     */
    @JsonProperty("video_url")
    private VideoUrl videoUrl;

    /**
     * 文件URL信息
     */
    @JsonProperty("file_url")
    private FileUrl fileUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {
        /**
         * 图片的Base64编码字符串
         * 格式必须为：data:image/[format];base64,[base64_data]
         * 例如：data:image/jpeg;base64,/9j/4AAQSkZJRgABA...
         *
         * 注意：本服务只支持Base64格式，不支持直接的URL链接
         *
         * 限制：
         * - 每张图像5M以下
         * - 像素不超过6000*6000
         * - 支持格式：jpg、png、jpeg
         */
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoUrl {
        /**
         * 视频的URL地址
         * GLM-4.5V视频大小限制为 200M 以内
         * GLM-4V-Plus视频大小限制为20M以内，视频时长不超过30s
         * 视频类型：mp4
         */
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUrl {
        /**
         * 文件的URL地址，不支持Base64编码
         * 支持PDF、Word等格式，最多支持50个
         */
        private String url;
    }
}