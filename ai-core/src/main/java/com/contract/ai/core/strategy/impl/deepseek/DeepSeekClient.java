package com.contract.ai.core.strategy.impl.deepseek;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * DeepSeek平台Feign客户端
 */
@FeignClient(
    name = "deepseek-client",
    url = "${ai.strategy.deepseek.base-url:https://api.deepseek.com}",
    configuration = DeepSeekConfiguration.class
)
public interface DeepSeekClient {

    /**
     * 调用DeepSeek平台聊天完成API
     *
     * @param request DeepSeek平台请求
     * @return DeepSeek平台响应
     */
    @PostMapping(
        value = "/v1/chat/completions",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    com.contract.ai.feign.dto.deepseek.DeepSeekChatResponse chatCompletions(
        @RequestBody com.contract.ai.feign.dto.deepseek.DeepSeekChatRequest request);
}