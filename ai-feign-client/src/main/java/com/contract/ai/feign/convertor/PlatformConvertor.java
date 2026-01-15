package com.contract.ai.feign.convertor;

import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;

/**
 * 平台转换器接口
 * 用于将标准的 ChatRequest/ChatResponse 转换为特定平台的格式
 * @param <T> 目标平台请求类型
 * @param <R> 目标平台响应类型
 */
public interface PlatformConvertor<T, R> {

    /**
     * 将标准 ChatRequest 转换为目标平台请求
     *
     * @param chatRequest 标准聊天请求
     * @return 目标平台请求对象
     */
    T convertRequest(ChatRequest chatRequest);

    /**
     * 将目标平台响应转换为标准 ChatResponse
     *
     * @param platformResponse 目标平台响应
     * @return 标准聊天响应
     */
    ChatResponse convertResponse(R platformResponse);
}