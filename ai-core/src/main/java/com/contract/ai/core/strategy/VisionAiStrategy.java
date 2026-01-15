package com.contract.ai.core.strategy;

import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视觉AI策略接口
 * 扩展AI策略以支持文件处理能力
 */
public interface VisionAiStrategy extends AiStrategy {

    /**
     * 处理带文件的聊天请求
     *
     * @param request 聊天请求
     * @param files 上传的文件列表
     * @return 聊天响应
     */
    ChatResponse handleChatWithVision(ChatRequest request, MultipartFile[] files);

    /**
     * 处理带base64图片的聊天请求
     *
     * @param request 聊天请求
     * @param imageMap 图片文件名到base64数据的映射
     * @return 聊天响应
     */
    ChatResponse handleChatWithVision(ChatRequest request, java.util.Map<String, String> imageMap);

    /**
     * 检查是否支持视觉功能
     *
     * @return 是否支持视觉功能
     */
    default boolean supportsVision() {
        return true;
    }
}