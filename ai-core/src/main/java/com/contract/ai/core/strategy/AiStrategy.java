package com.contract.ai.core.strategy;

import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;

import java.util.List;

/**
 * AI策略接口
 * 定义AI模型处理的标准接口
 */
public interface AiStrategy {

    /**
     * 获取支持的模型名称
     * 作为路由键使用
     *
     * @return 模型名称
     */
    List<String> getModel();

    /**
     * 处理聊天请求
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse handleChat(ChatRequest request);

    /**
     * 检查是否支持指定模型
     *
     * @param model 模型名称
     * @return 是否支持
     */
    default boolean supports(String model) {
        return getModel().equals(model);
    }

    /**
     * 获取策略优先级
     * 数值越小优先级越高
     *
     * @return 优先级
     */
    default int getPriority() {
        return 0;
    }
}